export class TruckEnterRecordGrid extends ListPage {
    btns: any[];
    toolbar: any;

    constructor(grid: any, queryform: any, toolbar: any) {
        super(grid, queryform, queryform.find('#query'), "/truckEnterRecord/listPage.action");
        this.toolbar = toolbar;
        this.btns = this.toolbar.find('button');
        window['truckInt']=this;
        // let categoryController:CategoryController=new CategoryController();
        // let cityController:CityController=new CityController();
        $('#detail-btn').on('click',async ()=>await this.openDetailPage());
        $('#add-btn').on('click',async () => await this.openAddPage());
        $('#edit-btn').on('click',async () => await this.openEditPage());
        // @ts-ignore
        $("#undo-btn").on('click',async ()=>await this.doDelete());
        this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());

    }

    private async openEditPage() {
        var row = this.rows[0];
        if(undefined==row){
            //@ts-ignore
            await bs4pop.alert("请选择一条数据", {type: 'warning'});
            return;
        }
        var url = this.toUrl('/truckEnterRecord/edit.html?id=' + row.id);
        //@ts-ignore
        var dia = bs4pop.dialog({
            title: '买家报备单详情',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '68%',
            height: '68%',
            btns: []
        });
    }

    private async doDelete() {
        var row = this.rows[0];
        if(undefined==row){
            //@ts-ignore
            await bs4pop.alert("请选择一条数据", {type: 'warning'});
            return;
        }
        //@ts-ignore
        bs4pop.removeAll();
        let promise = new Promise((resolve, reject) => {
            //@ts-ignore
            bs4pop.confirm('是否确认删除？<br/>',
                {type: 'warning',btns: [
                        {label: '是', className: 'btn-primary',onClick(cb){  resolve("true");}},
                        {label: '否', className: 'btn-cancel',onClick(cb){resolve("cancel");}},
                    ]});

        });
        let result = await promise;
        if(result=='cancel'){
            return;
        }
        let id =row.id;
        let url = this.toUrl("/truckEnterRecord/delete.action?id=" + id);
        try {
            var resp = await jq.ajaxWithProcessing({type: "GET", url: url, processData: true, dataType: "json"});
            if (!resp.success) {
                //@ts-ignore
                bs4pop.alert(resp.message, {type: 'error'});
                return;
            }
            await this.queryGridData();
            //@ts-ignore
            bs4pop.removeAll()
            //@ts-ignore
            bs4pop.alert('操作成功', {type: 'info', autoClose: 600});
        } catch (e) {
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
        }
    }

    //新增司机报备
    private async openAddPage() {
        console.log(1);
        let url = this.toUrl("/truckEnterRecord/edit.html");
        //@ts-ignore
        var dia  = bs4pop.dialog({
            title: '新增司机报备单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '50%',
            height: '68%',
            btns: []
        });
    }

    //查看司机报备单详情
    private async  openDetailPage(){
        var row=this.rows[0]
        var url=this.toUrl('/truckEnterRecord/view.html?id='+row.id);
        //@ts-ignore
        var dia = bs4pop.dialog({
            title: '司机报备单详情',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: []
        });
    }

    public removeAllAndLoadData() {
        //@ts-ignore
        bs4pop.removeAll();
        //@ts-ignore
        $("body").removeClass("modal-open");
        (async () => {
            await super.queryGridData();
        })();
    }

    public async doAudit(billId: string, code: string) {
        //@ts-ignore
        bs4pop.removeAll();
        let promise = new Promise((resolve, reject) => {
            //@ts-ignore
            bs4pop.confirm('是否审核通过当前登记单？<br/>' + code,
                {
                    type: 'warning', btns: [
                        {
                            label: '通过', className: 'btn-primary', onClick(cb) {
                                resolve("true");
                            }
                        },
                        {
                            label: '不通过', className: 'btn-primary', onClick(cb) {
                                resolve("false");
                            }
                        },
                        {
                            label: '取消', className: 'btn-cancel', onClick(cb) {
                                resolve("cancel");
                            }
                        },
                    ]
                });

        });
        let result = await promise; // wait until the promise resolves (*)
        if (result == 'cancel') {
            return;
        }
        let url = this.toUrl("/newRegisterBill/doAudit.action?id=" + billId + "&pass=" + result);

        try {
            var resp = await jq.ajaxWithProcessing({type: "GET", url: url, processData: true, dataType: "json"});
            if (!resp.success) {
                //@ts-ignore
                bs4pop.alert(resp.message, {type: 'error'});
                return;
            }
            await this.queryGridData();
            //@ts-ignore
            //TLOG.component.operateLog('登记单管理',"审核","【编号】:"+selected.code);
            //@ts-ignore
            bs4pop.removeAll()
            //@ts-ignore
            bs4pop.alert('操作成功', {type: 'info', autoClose: 600});
        } catch (e) {
            debugger
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
        }
    }

    private async doReviewCheck() {
        var selected = this.rows[0];
        let url = this.toUrl("/newRegisterBill/doReviewCheck.action?id=" + selected.id);
        let sure = await popwrapper.confirm('请确认是否复检？', undefined);
        if (!sure) {
            return;
        }

        try {
            var resp = await jq.ajaxWithProcessing({type: "GET", url: url, processData: true, dataType: "json"});
            if (!resp.success) {
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
            bs4pop.alert('操作成功', {type: 'info', autoClose: 600});
        } catch (e) {
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
        }


    }


    public async resetButtons() {
        var btnArray = this.btns;
        _.chain(btnArray).each((btn) => {
            //$(btn).hide();
        });
        await this.queryEventAndSetBtn();

    }

    private async queryEventAndSetBtn() {


    }


}
