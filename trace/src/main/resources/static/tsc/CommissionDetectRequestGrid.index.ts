class CommissionDetectRequestGrid extends ListPage {
    uid:string;
    btns:string[];
    toolbar:any;

    constructor(grid: any, queryform: any, toolbar: any) {
        super(grid,queryform,queryform.find('#query'),"/commissionDetectRequest/listPage.action");
        this.toolbar=toolbar;
        this.btns=this.toolbar.find('button');
        this.uid=_.uniqueId("trace_id_");

        $(window).on('resize',()=> this.grid.bootstrapTable('resetView') );

        var cthis=this;
        window['CommissionDetectRequestGridObj']=this;

        // 绑定按钮事件
        $('#assign-btn').on('click',async ()=>await this.openAssignPage());
        $('#sampling-btn').on('click',async ()=>await this.doSamplingCheck());
        $('#auto-btn').on('click',async ()=>await this.doAutoCheck());
        $('#review-btn').on('click',async ()=>await this.doReviewCheck());
        $('#undo-btn').on('click',async ()=>await this.doUndo());
        $('#detail-btn').on('click',async ()=>await this.openDetailPage());

        $('#add-btn').on('click', async () => await this.openCreatePage());
        $('#appointment-btn').on('click', async () => await this.audit());
        $('#createSheet-btn').on('click', async () => await this.doCreateCheckSheet());
        $('#batchReview-btn').on('click', async () => await this.doBatchReviewCheck());
        $('select[name="detectResultSelect"]').on('change',async (o,n)=>{
            var data=JSON.parse($('select[name="detectResultSelect"]').val().toString());
            $('input[name="detectType"]').val(data['detectType']);
            $('input[name="detectResult"]').val(data['detectResult']);
        });

        this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());
    }

    /**
     * 重置查询条件
     */
    public removeAllAndLoadData(){
        //@ts-ignore
        bs4pop.removeAll();
        //@ts-ignore
        $("body").removeClass("modal-open");
        (async ()=>{
            await this.queryGridData();
        })();
    }

    public isReviewCheck() {
        return this.findReviewCheckData().length > 0;
    }
    private findReviewCheckData() {
        return _.chain(this.rows).filter(item => {
            return DetectStatusEnum.FINISH_DETECT == item.detectStatus;
        }).filter(item => !_.isUndefined(item.detectRequest)).filter(item => {
            return DetectResultEnum.FAILED == item.detectRequest.detectResult
        }).value();
    }
    /**
     * 复检
     */
    public async doBatchReviewCheck() {

        if (!this.isReviewCheck()) {
            //@ts-ignore
            bs4pop.alert("没有数据可以进行批量复检", {type: 'warning'});
            return;
        }
        var arr = this.findReviewCheckData();

        let promise = new Promise((resolve, reject) => {
            // @ts-ignore
            layer.confirm('请确认是否复检选中数据？<br/>' + arr.map(e => e.code).join("<br\>"), {
                btn: ['确定', '取消'], title: "警告！！！",
                btn1: function () {
                    resolve(true);
                    return false;
                },
                btn2: function () {
                    resolve(false);
                    return false;
                }
            });
            $('.layui-layer').width('460px');
        });
        let result = await promise; // wait until the promise resolves (*)
        if (result) {
            // @ts-ignore
            var _url = ctx + "/commissionDetectRequest/doBatchReviewCheck.action";
            var idlist = arr.map(e => e.id);
            $.ajax({
                type: "POST",
                url: _url,
                data: JSON.stringify(idlist),
                processData: true,
                dataType: "json",
                contentType: 'application/json;charset=utf-8',
                async: true,
                success: function (data) {
                    if (data.code == "200") {
                        // @ts-ignore
                        TLOG.component.operateLog('登记单管理', "批量复检", '【IDS】:' + JSON.stringify(idlist));
                        // @ts-ignore
                        layer.alert('操作成功', {
                                title: '操作',
                                time: 600,
                                end: function () {
                                    // @ts-ignore
                                    layer.closeAll();
                                    // @ts-ignore
                                    queryRegisterBillGrid();
                                }
                            },
                            function () {
                                // @ts-ignore
                                layer.closeAll();
                                // @ts-ignore
                                queryRegisterBillGrid();
                            }
                        );

                    } else {
                        // @ts-ignore
                        layer.closeAll();
                        // @ts-ignore
                        popwrapper.alert(data.result)
                    }
                },
                error: function () {
                    // @ts-ignore
                    layer.closeAll();
                    popwrapper.alert("远程访问失败")
                }
            });
        }
        // @ts-ignore
        layer.closeAll();

    }

    /**
     * 创建打印报告
     */
    private doCreateCheckSheet() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            //@ts-ignore
            bs4pop.alert("请选择一条数据", {type: 'warning'});
            return;
        }
        var idList = row.map(function (v, i) {
            return v.billId
        });
        let param = $.param({idList: idList}, true);
        let url = this.toUrl("/checkSheet/edit.html?" + param);
        //@ts-ignore
        window.dia = bs4pop.dialog({
            title: '创建打印报告单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '80%',
            height: '70%',
            btns: [],
            onShowEnd: function () {
            }
        });
    }

    /**
     * 预约检测
     */
    private audit() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            //@ts-ignore
            bs4pop.alert("请选择一条数据", {type: 'warning'});
            return;
        }
        if (row.length > 1) {
            //@ts-ignore
            bs4pop.alert("请选择数据过多", {type: 'warning'});
            return;
        }
        console.log(row);
        let url = this.toUrl("/commissionDetectRequest/appointment.html?billId=" + row[0].billId);
        //@ts-ignore
        var audit_dia = bs4pop.dialog({
            title: '预约检测',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '80%',
            height: '70%',
            btns: [],
            onShowEnd: function () {
            }
        });

    }

    /**
     * 新增委托单
     */
    private openCreatePage() {
        let url = this.toUrl("/commissionDetectRequest/create.html");
        //@ts-ignore
        var dia = bs4pop.dialog({
            title: '新增委托单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '78%',
            height: '98%',
            btns: [],
            onShowEnd: function () {
            }
        });
    }

    /**
     * 进入分配检测员页面
     */
    private async  openAssignPage(){
        var row=this.rows[0]
        if(_.isUndefined(row.billId)||row.billId==null){
            //@ts-ignore
            bs4pop.alert('请行进行预约检测', {type: 'error'});
            return;
        }
        var url=this.toUrl('/commissionDetectRequest/confirm.html?billId='+row.billId);
        //@ts-ignore
        var dia = bs4pop.dialog({
            title: '接单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '80%',
            height: '60%',
            btns: []
        });
    }

    /**
     * 检测接单
     * @param id
     * @param designatedId
     * @param designatedName
     */
    public async doAssign(billId:string, designatedId:string, designatedName:string, detectTime:string){
        //@ts-ignore
        bs4pop.removeAll();
        let promise = new Promise((resolve, reject) => {
            //@ts-ignore
            bs4pop.confirm('是否确认接单？<br/>',
                {type: 'warning',btns: [
                        {label: '是', className: 'btn-primary',onClick(cb){  resolve("true");}},
                        {label: '否', className: 'btn-cancel',onClick(cb){resolve("cancel");}},
                    ]});

        });
        let result = await promise;
        if(result=='cancel'){
            return;
        }

        let url= this.toUrl("/customerDetectRequest/doConfirm.action?billId="+billId+"&designatedId="+designatedId+"&designatedName="+designatedName+"&detectTime="+detectTime);
        try{
            var resp=await jq.ajaxWithProcessing({type: "GET",url: url,processData:true,dataType: "json"});
            if(!resp.success){
                //@ts-ignore
                bs4pop.alert(resp.message, {type: 'error'});
                return;
            }
            await this.queryGridData();
            //@ts-ignore
            bs4pop.removeAll()
            //@ts-ignore
            bs4pop.alert('操作成功', {type: 'info',autoClose: 600});
        }catch (e){
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
        }
    }

    /**
     * 退回
     * @param id
     */
    public async returnAssign(billId:string){
        //@ts-ignore
        bs4pop.removeAll();
        let promise:Promise<string> = new Promise<string>((resolve, reject) => {
            //@ts-ignore
            bs4pop.prompt('请输入退回原因:', '',{
                title: '是否确认退回？',
                hideRemove: true,
                width: 500,
                className:"max20",
                onShowEnd: function(){
                    $(".max20").find("input").attr("maxlength",20);
                },
            }, function(sure, value){
                if(sure==true){
                    if($.trim(value)==''){
                        //@ts-ignore
                        bs4pop.alert('请输入退回原因', {type: 'error'});
                        return
                    }
                    resolve(value);
                }else{
                    reject(value);
                }
            });
        });
        let data={id:billId,returnReason:''};
        try{
            data.returnReason = await promise;
        }catch (e) {
            return;
        }
        let url= this.toUrl("/customerDetectRequest/doReturn.action?billId="+billId);
        try{
            let resp=await jq.postJsonWithProcessing( url,data);
            if(!resp.success){
                //@ts-ignore
                bs4pop.alert(resp.message, {type: 'error'});
                return;
            }
            await this.queryGridData();
            //@ts-ignore
            bs4pop.removeAll()
            //@ts-ignore
            bs4pop.alert('操作成功', {type: 'info',autoClose: 600});
        }catch (e){
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
        }
    }

    /**
     * 采样检测
     */
    private async  doSamplingCheck() {
        var selected = this.rows[0];
        var url= this.toUrl( "/newRegisterBill/doSamplingCheck.action?id="+ selected.billId);
        let sure=await popwrapper.confirm('请确认是否采样检测？',undefined);
        if(!sure){
            return;
        }
        try{
            var resp=await jq.ajaxWithProcessing({type: "GET",url: url,processData:true,dataType: "json"});
            if(!resp.success){
                //@ts-ignore
                bs4pop.alert(resp.message, {type: 'error'});
                return;
            }
            await this.queryGridData();
            //@ts-ignore
            bs4pop.removeAll()
            //@ts-ignore
            bs4pop.alert('操作成功', {type: 'info',autoClose: 600});
        }catch (e){
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
        }
    }

    /**
     * 主动送检
     */
    private async doAutoCheck() {
        var selected = this.rows[0];
        let cthis=this;
        var url= this.toUrl( "/newRegisterBill/doAutoCheck.action?id="+ selected.billId);
        let sure=await popwrapper.confirm('请确认是否主动送检？',undefined);
        if(!sure){
            return;
        }

        try{
            var resp=await jq.ajaxWithProcessing({type: "GET",url: url,processData:true,dataType: "json"});
            if(!resp.success){
                //@ts-ignore
                bs4pop.alert(resp.message, {type: 'error'});
                return;
            }
            await cthis.queryGridData();
            //@ts-ignore
            //TLOG.component.operateLog('登记单管理',"主动送检","【编号】:"+selected.code);
            //@ts-ignore
            bs4pop.removeAll()
            //@ts-ignore
            bs4pop.alert('操作成功', {type: 'info',autoClose: 600});
        }catch (e){
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
        }
    }

    /**
     * 复检
     */
    private async  doReviewCheck() {
        var selected = this.rows[0];
        let url= this.toUrl("/newRegisterBill/doReviewCheck.action?id="+ selected.billId);
        let sure=await popwrapper.confirm('请确认是否复检？',undefined);
        if(!sure){
            return;
        }

        try{
            var resp=await jq.ajaxWithProcessing({type: "GET",url: url,processData:true,dataType: "json"});
            if(!resp.success){
                //@ts-ignore
                bs4pop.alert(resp.message, {type: 'error'});
                return;
            }
            await this.queryGridData();
            //@ts-ignore
            //TLOG.component.operateLog('登记单管理',"复检","【编号】:"+selected.code);
            //@ts-ignore
            bs4pop.removeAll()
            //@ts-ignore
            bs4pop.alert('操作成功', {type: 'info',autoClose: 600});
        }catch (e){
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
        }
    }

    /**
     * 撤销检测请求
     */
    private async doUndo(){
        let selected = this.rows[0];
        let cthis=this;
        let url= this.toUrl( "/customerDetectRequest/doUndo.action?id="+ selected.id);
        //@ts-ignore
        bs4pop.confirm('请确认是否撤销？', undefined, async function (sure) {
            if(!sure){
                return;
            }
            try{
                var resp=await jq.ajaxWithProcessing({type: "GET",url: url,processData:true,dataType: "json"});

                if(!resp.success){
                    //@ts-ignore
                    bs4pop.alert(resp.message, {type: 'error'});
                    return;
                }
                await cthis.queryGridData();
                //@ts-ignore
                //TLOG.component.operateLog('登记单管理',"撤销","【编号】:"+selected.code);
                //@ts-ignore
                bs4pop.removeAll()
                //@ts-ignore
                bs4pop.alert('操作成功', {type: 'info',autoClose: 600});
            }catch (e){
                //@ts-ignore
                bs4pop.alert('远程访问失败', {type: 'error'});
            }
        });
    }

    /**
     * 查看详情
     */
    private async  openDetailPage(){
        var row=this.rows[0]
        var url=this.toUrl('/commissionDetectRequest/view.html?id='+row.billId);
        //@ts-ignore
        var dia = bs4pop.dialog({
            title: '报备单详情',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: []
        });
    }

    get rows() {
        return  this.grid.bootstrapTable('getSelections');
    }

    public async resetButtons() {
        var btnArray=this.btns;
        _.chain(btnArray).each((btn)=> {
            $(btn).hide();
        });
        await this.queryEventAndSetBtn();
        $("#add-btn").show();
        $("#export").show();
    }

    private async queryEventAndSetBtn(){
        var rows=this.rows;
        try{
            var billIdList=_.chain(rows).map(v=>v.billId).value();
            var resp=await jq.postJson(this.toUrl('/customerDetectRequest/queryEvents.action'),billIdList);
            resp.forEach(btnid=>{ $('#'+btnid).show();})
        }catch (e){
            console.error(e);
        }

    }

}
