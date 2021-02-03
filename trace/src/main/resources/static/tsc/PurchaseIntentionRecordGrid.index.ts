export class PurchaseIntentionRecordGrid extends ListPage {
    uid:string;
    highLightBill: any;
    btns:any[];
    toolbar:any;

    constructor(grid: any, queryform: any, toolbar: any) {
        super(grid, queryform, queryform.find('#query'), "/purchaseIntentionRecord/listPage.action");
        this.toolbar = toolbar;
        this.btns = this.toolbar.find('button');
        window['purchaseInt'] = this;

        //@ts-ignore
        $('#detail-btn').on('click',async ()=>await this.openDetailPage())
        //@ts-ignore
        $("#btn_add").on('click',async ()=>await this.openAddPage())
        //@ts-ignore
        $("#edit-btn").on('click',async ()=>await this.openEditPage())
        // @ts-ignore
        $("#undo-btn").on('click',async ()=>await this.doDelete())

        this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());

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
        let url = this.toUrl("/purchaseIntentionRecord/doDelete.action?id=" + id);
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

    private async openEditPage() {
        var row = this.rows[0];
        if(undefined==row){
            //@ts-ignore
            await bs4pop.alert("请选择一条数据", {type: 'warning'});
            return;
        }
        var url = this.toUrl('/purchaseIntentionRecord/edit.html?id=' + row.id);
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

    private async openAddPage() {
        var url = this.toUrl('/purchaseIntentionRecord/add.html');
        //@ts-ignore
        var add_dia = bs4pop.dialog({
            title: '新增买家报备单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '68%',
            height: '58%',
            btns: []
        });
    }

    private async openDetailPage() {
        var row = this.rows[0]
        var url = this.toUrl('/purchaseIntentionRecord/view.html?id=' + row.id);
        //@ts-ignore
        var dia = bs4pop.dialog({
            title: '买家报备单详情',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '88%',
            height: '58%',
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
        //@ts-ignore
        _.chain(btnArray).each((btn) => {
            //$(btn).hide();
        });
        await this.queryEventAndSetBtn();

    }

    private async queryEventAndSetBtn() {


    }


}
