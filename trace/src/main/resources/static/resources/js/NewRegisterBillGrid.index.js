class NewRegisterBillGrid extends ListPage {
    constructor(grid, queryform, toolbar) {
        super(grid, queryform, queryform.find('#query'), "/newRegisterBill/listPage.action");
        this.toolbar = toolbar;
        this.btns = this.toolbar.find('button');
        this.uid = _.uniqueId("trace_id_");
        let categoryController = new CategoryController();
        let cityController = new CityController();
        this.initTraceAutoComplete($("[name='productName']"), function (query, done) { categoryController.lookupCategories(query, done); });
        this.initTraceAutoComplete($("[name='originName']"), function (query, done) { cityController.lookupCities(query, done); });
        $(window).on('resize', () => this.grid.bootstrapTable('resetView'));
        var cthis = this;
        window['RegisterBillGridObj'] = this;
        $('#edit-btn').on('click', async () => await this.openEditPage());
        $('#btn_add').on('click', async () => await this.openCreatePage());
        $('#copy-btn').on('click', async () => await this.openCopyPage());
        $('#upload-origincertifiy-btn').on('click', async () => await this.openUploadOriginCertifyPage());
        $('#upload-detectreport-btn').on('click', async () => await this.openUploadDetectReportPage());
        $('#upload-handleresult-btn').on('click', async () => await this.openUploadHandleResultPage());
        $('#detail-btn').on('click', async () => await this.openDetailPage());
        $('#audit-btn').on('click', async () => await this.openAuditPage());
        $('#undo-btn').on('click', async () => await this.doUndo());
        $('#batch-undo-btn').on('click', async () => await this.doBatchUndo());
        $('#batch-audit-btn').on('click', async () => await this.doBatchAudit());
        $('#createsheet-btn').on('click', async () => await this.openCreateSheetPage());
        $('#sampling-btn').on('click', async () => await this.doSamplingCheck());
        $('#batch-auto-btn').on('click', async () => await this.doBatchAutoCheck());
        $('#auto-btn').on('click', async () => await this.doAutoCheck());
        $('#remove-reportAndcertifiy-btn').on('click', async () => await this.doRemoveReportAndCertifiy());
        $('#batch-sampling-btn').on('click', async () => await this.doBatchSamplingCheck());
        $('#audit-withoutDetect-btn').on('click', async () => await this.doAuditWithoutDetect());
        $('#review-btn').on('click', async () => await this.doReviewCheck());
        $('#update-img-btn').on('click', async () => await this.doUpdateImg());
        $('#btn_checkin').on('click', async () => await this.openCheckinPage());
        $('select[name="detectResultSelect"]').on('change', async (o, n) => {
            var data = JSON.parse($('select[name="detectResultSelect"]').val().toString());
            $('input[name="detectType"]').val(data['detectType']);
            $('input[name="detectResult"]').val(data['detectResult']);
        });
        this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());
        this.grid.bootstrapTable({
            onLoadSuccess: async () => {
                await cthis.findHighLightBill();
            }
        });
        window.addEventListener('message', function (e) {
            var data = JSON.parse(e.data);
            if (data.obj && data.fun) {
                if (data.obj == 'RegisterBillGridObj') {
                    cthis[data.fun].call(cthis, data.args);
                }
            }
        }, false);
    }
    removeAllAndLoadData() {
        bs4pop.removeAll();
        $("body").removeClass("modal-open");
        (async () => {
            await super.queryGridData();
        })();
    }
    async doUpdateImg() {
        let row = this.rows[0];
        let url = this.toUrl('/newRegisterBill/update_image.html?id=' + row.billId);
        var update_img_dia = bs4pop.dialog({
            title: '修改图片',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '88%',
            height: '98%',
            btns: []
        });
    }
    async doAudit(billId, code) {
        bs4pop.removeAll();
        let promise = new Promise((resolve, reject) => {
            bs4pop.confirm('是否审核通过当前登记单？<br/>' + code, { type: 'warning', btns: [
                    { label: '通过', className: 'btn-primary', onClick(cb) { resolve(20); } },
                    { label: '不通过', className: 'btn-primary', onClick(cb) { resolve(30); } },
                    { label: '退回', className: 'btn-primary', onClick(cb) { resolve(10); } },
                    { label: '取消', className: 'btn-cancel', onClick(cb) { resolve("cancel"); } },
                ] });
        });
        let result = await promise;
        if (result == 'cancel') {
            return;
        }
        let url = this.toUrl("/newRegisterBill/doAudit.action?id=" + billId + "&verifyStatus=" + result);
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
    async doAuditWithoutDetect() {
        var selected = this.rows[0];
        let url = this.toUrl("/newRegisterBill/doAuditWithoutDetect.action");
        let sure = await popwrapper.confirm('确认审核不检测？', undefined);
        if (!sure) {
            return;
        }
        try {
            var resp = await jq.ajaxWithProcessing({ type: "POST", data: { id: selected.id }, url: url, processData: true, dataType: "json" });
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
    async doBatchSamplingCheck() {
        var rows = this.rows;
        if (this.batchSamplingRows.length == 0) {
            layer.alert('所选登记单子不能采样检测');
            return;
        }
        var codeList = [];
        var batchIdList = [];
        for (var i = 0; i < this.batchSamplingRows.length; i++) {
            batchIdList.push(this.batchSamplingRows[i].id);
            codeList.push(this.batchSamplingRows[i].code);
        }
        let sure = await popwrapper.confirm('批量采样检测<br\>' + codeList.join("<br\>"), undefined);
        if (!sure) {
            return;
        }
        let url = this.toUrl("/newRegisterBill/doBatchSamplingCheck.action");
        try {
            var resp = await jq.postJsonWithProcessing(url, batchIdList);
            if (!resp.success) {
                bs4pop.alert(resp.message, { type: 'error' });
                return;
            }
            if (!resp.success) {
                bs4pop.alert(resp.message, { type: 'error' });
                return;
            }
            var failureList = resp.data.failureList;
            if (failureList.length > 0) {
                bs4pop.alert('成功:' + resp.data.successList.join('</br>') + '失败:' + resp.data.failureList.join('</br>'), { type: 'warning' });
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
    async doRemoveReportAndCertifiy() {
        var selected = this.rows[0];
        let promise = new Promise((resolve, reject) => {
            bs4pop.confirm('所选登记单子不能主动送检', { type: 'warning', btns: [
                    { label: '全部删除', className: 'btn-primary', onClick(cb) { resolve("all"); } },
                    { label: '删除产地证明', className: 'btn-primary', onClick(cb) { resolve("originCertifiy"); } },
                    { label: '删除检测报告', className: 'btn-primary', onClick(cb) { resolve("detectReport"); } },
                    { label: '取消', className: 'btn-cancel', onClick(cb) { resolve("cancel"); } },
                ] });
        });
        let result = await promise;
        var _url = this.toUrl("/newRegisterBill/doRemoveReportAndCertifiy.action");
        if (result != 'cancel') {
            try {
                var resp = await jq.postJsonWithProcessing(_url, { id: selected.id, deleteType: result });
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
        else {
            layer.closeAll();
        }
    }
    async doAutoCheck() {
        var selected = this.rows[0];
        let cthis = this;
        var url = this.toUrl("/newRegisterBill/doAutoCheck.action?id=" + selected.id);
        let sure = await popwrapper.confirm('请确认是否主动送检？', undefined);
        if (!sure) {
            return;
        }
        try {
            var resp = await jq.ajaxWithProcessing({ type: "GET", url: url, processData: true, dataType: "json" });
            if (!resp.success) {
                bs4pop.alert(resp.message, { type: 'error' });
                return;
            }
            await cthis.queryGridData();
            bs4pop.removeAll();
            bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
        }
        catch (e) {
            bs4pop.alert('远程访问失败', { type: 'error' });
        }
    }
    async doBatchAutoCheck() {
        if (this.batchSamplingRows.length == 0) {
            bs4pop.alert('所选登记单子不能主动送检', { type: 'warning' });
            return;
        }
        var codeList = [];
        var batchIdList = [];
        for (var i = 0; i < this.batchSamplingRows.length; i++) {
            batchIdList.push(this.batchSamplingRows[i].id);
            codeList.push(this.batchSamplingRows[i].code);
        }
        var url = this.toUrl("/newRegisterBill/doBatchAutoCheck.action");
        let sure = await popwrapper.confirm('请确认是否主动送检？', undefined);
        if (!sure) {
            return;
        }
        try {
            var resp = await jq.postJsonWithProcessing(url, batchIdList);
            if (!resp.success) {
                bs4pop.alert(resp.message, { type: 'error' });
                return;
            }
            var failureList = resp.data.failureList;
            if (failureList.length > 0) {
                bs4pop.alert('成功:' + resp.data.successList.join('</br>') + '失败:' + resp.data.failureList.join('</br>'), { type: 'warning' });
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
    async doSamplingCheck() {
        var selected = this.rows[0];
        var url = this.toUrl("/newRegisterBill/doSamplingCheck.action?id=" + selected.id);
        let sure = await popwrapper.confirm('请确认是否采样检测？', undefined);
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
    async openCreateSheetPage() {
        var rows = this.rows;
        var idList = rows.map(function (v, i) { return v.id; });
        let url = this.toUrl('/checkSheet/edit.html?' + $.param({ idList: idList }, true));
        window.dia = bs4pop.dialog({
            title: '打印检测报告',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: []
        });
    }
    async doUndo() {
        let selected = this.rows[0];
        let cthis = this;
        let url = this.toUrl("/newRegisterBill/doUndo.action?id=" + selected.id);
        bs4pop.confirm('请确认是否撤销？', undefined, async function (sure) {
            if (!sure) {
                return;
            }
            try {
                var resp = await jq.ajaxWithProcessing({ type: "GET", url: url, processData: true, dataType: "json" });
                if (!resp.success) {
                    bs4pop.alert(resp.message, { type: 'error' });
                    return;
                }
                await cthis.queryGridData();
                bs4pop.removeAll();
                bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
            }
            catch (e) {
                bs4pop.alert('远程访问失败', { type: 'error' });
            }
        });
    }
    async doBatchAudit() {
        var waitAudit = this.filterByProp('verifyStatus', [0]);
        if (waitAudit.length == 0) {
            bs4pop.alert('所选登记单不能进行审核', { type: 'error' });
            return;
        }
        var codeList = [];
        var batchIdList = [];
        var onlyWithOriginCertifiyUrlIdList = [];
        for (var i = 0; i < waitAudit.length; i++) {
            batchIdList.push(waitAudit[i].id);
            codeList.push(waitAudit[i].code);
            if (waitAudit[i].hasOriginCertifiy != 0 && waitAudit[i].hasDetectReport == 0) {
                onlyWithOriginCertifiyUrlIdList.push(waitAudit[i].code);
            }
            ;
        }
        let promise = new Promise((resolve, reject) => {
            bs4pop.confirm('批量审核<br\>' + codeList.join("<br\>"), undefined, async function (sure) {
                resolve(sure);
            });
        });
        let result = await promise;
        if (result == true) {
            var passWithOriginCertifiyUrl = null;
            if (onlyWithOriginCertifiyUrlIdList.length > 0) {
                let reconfirmPromise = new Promise((resolve, reject) => {
                    bs4pop.confirm('只有产地证明登记单:<br/>' + onlyWithOriginCertifiyUrlIdList.join("<br\>"), { btn: ['不检测', '检测'], title: "是否不再进行检测" }, function () {
                        resolve(true);
                    }, function () {
                        resolve(false);
                    });
                });
                passWithOriginCertifiyUrl = await reconfirmPromise;
            }
            bs4pop.removeAll();
            var cthis = this;
            try {
                let url = this.toUrl("/newRegisterBill/doBatchAudit.action");
                let reqData = JSON.stringify({ registerBillIdList: batchIdList, verifyStatus: true, passWithOriginCertifiyUrl: passWithOriginCertifiyUrl });
                var resp = await jq.ajaxWithProcessing({ type: "POST", data: reqData, url: url, processData: true, dataType: "json", contentType: 'application/json;charset=utf-8' });
                if (!resp.success) {
                    bs4pop.alert(resp.message, { type: 'error' });
                    return;
                }
                await cthis.queryGridData();
                bs4pop.removeAll();
                bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
            }
            catch (e) {
                bs4pop.alert('远程访问失败', { type: 'error' });
            }
        }
    }
    async openAuditPage() {
        var row = this.rows[0];
        var url = this.toUrl('/newRegisterBill/audit.html?id=' + row.billId);
        var dia = bs4pop.dialog({
            title: '进场审核',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '80%',
            height: '60%',
            btns: []
        });
    }
    async openDetailPage() {
        var row = this.rows[0];
        var url = this.toUrl('/newRegisterBill/view.html?id=' + row.billId);
        layer.open({
            type: 2,
            title: '报备单详情',
            area: ['85%', '80%'],
            shade: 0.8,
            closeBtn: 0,
            shadeClose: true,
            content: url,
            btn: ['关闭'],
            yes: function (index) {
                if (layer) {
                    layer.close(index);
                }
            },
        });
    }
    async openUploadHandleResultPage() {
        var row = this.rows[0];
        var url = this.toUrl('/newRegisterBill/uploadHandleResult.html?id=' + row.billId);
        var dia = bs4pop.dialog({
            title: '上传处理结果',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: []
        });
    }
    async openUploadDetectReportPage() {
        var row = this.rows[0];
        var url = this.toUrl('/newRegisterBill/uploadDetectReport.html?id=' + row.billId);
        var dia = bs4pop.dialog({
            title: '上传检测报告',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: []
        });
    }
    async openUploadOriginCertifyPage() {
        var row = this.rows[0];
        var url = this.toUrl('/newRegisterBill/uploadOrigincertifiy.html?id=' + row.billId);
        var dia = bs4pop.dialog({
            title: '上传产地证明',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: []
        });
    }
    openCopyPage() {
        var row = this.rows[0];
        let url = this.toUrl("/newRegisterBill/copy.html?id=" + row.billId);
        var dia = bs4pop.dialog({
            title: '复制报备单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: []
        });
    }
    async openCheckinPage() {
        let url = this.toUrl("/newRegisterBill/add.html");
        let sure = await popwrapper.confirm('确认要进门当前报备单吗?', undefined);
        if (!sure) {
            return;
        }
        var cthis = this;
        try {
            bs4pop.removeAll();
            var row = this.rows[0];
            let url = this.toUrl("/newRegisterBill/doCheckIn.action");
            var resp = await jq.postJsonWithProcessing(url, { id: row.id }, {});
            if (!resp.success) {
                bs4pop.alert(resp.message, { type: 'error' });
                return;
            }
            await cthis.queryGridData();
            bs4pop.removeAll();
            bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
        }
        catch (e) {
            bs4pop.alert('远程访问失败', { type: 'error' });
        }
    }
    openCreatePage() {
        let url = this.toUrl("/newRegisterBill/add.html");
        var createDia = bs4pop.dialog({
            title: '新增报备单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: [],
            onShowEnd: function () {
            }
        });
    }
    openEditPage() {
        var row = this.rows[0];
        let url = this.toUrl("/newRegisterBill/edit.html?id=" + row.billId);
        var dia = bs4pop.dialog({
            title: '修改报备单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '98%',
            height: '98%',
            btns: []
        });
    }
    async resetButtons() {
        var btnArray = this.btns;
        _.chain(btnArray).each((btn) => {
            $(btn).hide();
        });
        await this.queryEventAndSetBtn();
    }
    async queryEventAndSetBtn() {
        var rows = this.rows;
        try {
            var billIdList = _.chain(rows).map(v => v.id).value();
            var resp = await jq.postJson(this.toUrl('/newRegisterBill/queryEvents.action'), billIdList);
            resp.forEach(btnid => { $('#' + btnid).show(); });
        }
        catch (e) {
            console.error(e);
        }
    }
    get waitAuditRows() {
        return this.filterByProp('verifyStatus', [0]);
    }
    get batchSamplingRows() {
        return this.filterByProp('detectStatus', [20]);
    }
    multiRows() {
        if (this.rows.length <= 1) {
            return;
        }
    }
    singleRow() {
        if (this.rows.length != 1) {
            return;
        }
    }
    async findHighLightBill() {
        try {
            let url = this.toUrl("/newRegisterBill/findHighLightBill.action");
            return await jq.postJson(url, {}, {});
        }
        catch (e) {
            console.log(e);
            return {};
        }
    }
    static queryProduct(param, success, error) {
        var productName = $('#productCombobox').combotree('getText');
        var data = [];
        var url = '/toll/category.action?name=' + productName;
        $.ajax({
            url: url,
            success: function (resp) {
                data = resp.suggestions.map(function (v, i) {
                    v['parentId'] = '';
                    return v;
                });
                success(data);
            }
        });
    }
    async doBatchUndo() {
        var cthis = this;
        var waitAudit = this.waitAuditRows;
        if (waitAudit.length == 0) {
            bs4pop.alert('所选登记单不能进行撤销', { type: 'error' });
            return;
        }
        let promise = new Promise((resolve, reject) => {
            bs4pop.confirm('请确认是否撤销选中数据？<br\>' + waitAudit.map(e => e.code).join("<br\>"), undefined, async function (sure) {
                resolve(sure);
            });
        });
        let result = await promise;
        if (result == true) {
            try {
                bs4pop.removeAll();
                let url = this.toUrl("/newRegisterBill/doBatchUndo.action");
                var idlist = waitAudit.map(e => e.id);
                var resp = await jq.ajaxWithProcessing({ type: "POST", data: JSON.stringify(idlist), url: url, processData: true, dataType: "json", contentType: 'application/json;charset=utf-8' });
                if (!resp.success) {
                    bs4pop.alert(resp.message, { type: 'error' });
                    return;
                }
                await cthis.queryGridData();
                bs4pop.removeAll();
                bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
            }
            catch (e) {
                bs4pop.alert('远程访问失败', { type: 'error' });
            }
        }
    }
    filterByProp(prop, propValues) {
        let arrayData = $.makeArray(this.rows);
        let arrayValue = $.makeArray(propValues);
        let values = _.chain(arrayData).filter(element => $.inArray(element[prop], arrayValue) > -1).value();
        return values;
    }
}
