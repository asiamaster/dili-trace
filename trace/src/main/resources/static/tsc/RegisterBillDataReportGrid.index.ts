class RegisterBillDataReportGrid extends ListPage {
    uid:string;
    btns:any[];
    toolbar:any;

    constructor(grid: any, queryform: any, toolbar: any) {
        super(grid,queryform,queryform.find('#query'),"/registerBillDataReport/listStaticsPage.action");
        this.toolbar=toolbar;
        this.btns=this.toolbar.find('button');
        this.uid=_.uniqueId("trace_id_");
        $(window).on('resize',()=> this.grid.bootstrapTable('resetView') );

        let cthis=this;
        window['DetectReportStaticsGridObj']=this;

        $('#detail-btn').on('click',async ()=>await this.openDetailPage())
        $("#registerSource").on('change',async ()=>await this.changeType())
        // this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());
        //追加监听统计各个类型的数量
        document.getElementById("query").addEventListener("click", function () {
            cthis.initStaticsNum("/registerBillDataReport/listStaticsPageNum.action");
        }, false);
        window.addEventListener('message', function(e) {
            var data=JSON.parse(e.data);
            if(data.obj&&data.fun){
                if(data.obj=='DetectReportStaticsGridObj'){
                    cthis[data.fun].call(cthis,data.args)
                }
           }
        }, false);

        this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());

        (async ()=>{
            await this.initStaticsNum("/registerBillDataReport/listStaticsPageNum.action");
        })();
    }

    public removeAllAndLoadData() {
        //@ts-ignore
        bs4pop.removeAll();
        //@ts-ignore
        $("body").removeClass("modal-open");
        (async ()=>{
            await super.queryGridData();
        })();
    }

    public async resetButtons(){
        var rows=this.rows;
        if(rows.length==1){
            $("#detail-btn").show();
        }else{
            $("#detail-btn").hide();
        }
    }

    public async initStaticsNum(pageUrl){
        let staticsData = this.queryform.serializeJSON();
        let url=this.toUrl(pageUrl)
        $.ajax({
            type: "POST",
            url: url,
            data:JSON.stringify(staticsData),
            processData:true,
            dataType: "json",
            async : false,
            contentType: "application/json; charset=utf-8",
            success: function (ret) {
                if(ret){
                    if(ret.data==undefined){return}
                    $.each(ret.data,function(filed,val){
                        // @ts-ignore
                        $("#"+filed).val(val);
                    });
                }
            },
            error: function(e){
                console.log(e);
            }
        });
    }

    private async changeType(){
        let changeVal = $("#registerSource").val();
        if(changeVal==2){
            $("[name='tradeTypeId']").removeAttr("disabled")
        }else{
            $("[name='tradeTypeId']").val("");
            $("[name='tradeTypeId']").attr("disabled","disabled")
        }
    }

    private async  openDetailPage() {
        var row=this.rows[0]
        var url=this.toUrl('/newRegisterBill/view.html?id='+row.billId);
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

}
