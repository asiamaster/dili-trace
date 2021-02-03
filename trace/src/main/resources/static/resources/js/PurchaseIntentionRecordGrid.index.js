(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.PurchaseIntentionRecordGrid = void 0;
    class PurchaseIntentionRecordGrid extends ListPage {
        constructor(grid, queryform, toolbar) {
            super(grid, queryform, queryform.find('#query'), "/purchaseIntentionRecord/listPage.action");
            this.toolbar = toolbar;
            this.btns = this.toolbar.find('button');
            window['purchaseInt'] = this;
            $('#detail-btn').on('click', async () => await this.openDetailPage());
            $("#btn_add").on('click', async () => await this.openAddPage());
            $("#edit-btn").on('click', async () => await this.openEditPage());
            $("#undo-btn").on('click', async () => await this.doDelete());
            this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());
        }
        async doDelete() {
            var row = this.rows[0];
            if (undefined == row) {
                await bs4pop.alert("请选择一条数据", { type: 'warning' });
                return;
            }
            bs4pop.removeAll();
            let promise = new Promise((resolve, reject) => {
                bs4pop.confirm('是否确认删除？<br/>', { type: 'warning', btns: [
                        { label: '是', className: 'btn-primary', onClick(cb) { resolve("true"); } },
                        { label: '否', className: 'btn-cancel', onClick(cb) { resolve("cancel"); } },
                    ] });
            });
            let result = await promise;
            if (result == 'cancel') {
                return;
            }
            let id = row.id;
            let url = this.toUrl("/purchaseIntentionRecord/doDelete.action?id=" + id);
            try {
                var resp = await jq.ajaxWithProcessing({ type: "GET", url: url, processData: true, dataType: "json" });
                if (!resp.success) {
                    bs4pop.alert(resp.message, { type: 'error' });
                    return;
                }
                await this.queryGridData();
                bs4pop.removeAll();
                bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
            }
            catch (e) {
                bs4pop.alert('远程访问失败', { type: 'error' });
            }
        }
        async openEditPage() {
            var row = this.rows[0];
            if (undefined == row) {
                await bs4pop.alert("请选择一条数据", { type: 'warning' });
                return;
            }
            var url = this.toUrl('/purchaseIntentionRecord/edit.html?id=' + row.id);
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
        async openAddPage() {
            var url = this.toUrl('/purchaseIntentionRecord/add.html');
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
        async openDetailPage() {
            var row = this.rows[0];
            var url = this.toUrl('/purchaseIntentionRecord/view.html?id=' + row.id);
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
        removeAllAndLoadData() {
            bs4pop.removeAll();
            $("body").removeClass("modal-open");
            (async () => {
                await super.queryGridData();
            })();
        }
        async doAudit(billId, code) {
            bs4pop.removeAll();
            let promise = new Promise((resolve, reject) => {
                bs4pop.confirm('是否审核通过当前登记单？<br/>' + code, {
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
            let result = await promise;
            if (result == 'cancel') {
                return;
            }
            let url = this.toUrl("/newRegisterBill/doAudit.action?id=" + billId + "&pass=" + result);
            try {
                var resp = await jq.ajaxWithProcessing({ type: "GET", url: url, processData: true, dataType: "json" });
                if (!resp.success) {
                    bs4pop.alert(resp.message, { type: 'error' });
                    return;
                }
                await this.queryGridData();
                bs4pop.removeAll();
                bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
            }
            catch (e) {
                debugger;
                bs4pop.alert('远程访问失败', { type: 'error' });
            }
        }
        async doReviewCheck() {
            var selected = this.rows[0];
            let url = this.toUrl("/newRegisterBill/doReviewCheck.action?id=" + selected.id);
            let sure = await popwrapper.confirm('请确认是否复检？', undefined);
            if (!sure) {
                return;
            }
            try {
                var resp = await jq.ajaxWithProcessing({ type: "GET", url: url, processData: true, dataType: "json" });
                if (!resp.success) {
                    bs4pop.alert(resp.message, { type: 'error' });
                    return;
                }
                await this.queryGridData();
                bs4pop.removeAll();
                bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
            }
            catch (e) {
                bs4pop.alert('远程访问失败', { type: 'error' });
            }
        }
        async resetButtons() {
            var btnArray = this.btns;
            _.chain(btnArray).each((btn) => {
            });
            await this.queryEventAndSetBtn();
        }
        async queryEventAndSetBtn() {
        }
    }
    exports.PurchaseIntentionRecordGrid = PurchaseIntentionRecordGrid;
});
