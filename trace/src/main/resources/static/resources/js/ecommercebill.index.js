class EcommerceBillGrid extends ListPage {
    constructor(grid, queryform, toolbar) {
        super(grid, queryform, queryform.find('#query'), "/ecommerceBill/listPage.action");
        this.grid = grid;
        this.queryform = queryform;
        this.toolbar = toolbar;
        let categoryController = new CategoryController();
        let cityController = new CityController();
        window['ECommerceBillGrid'] = this;
        this.btns = this.toolbar.find('button');
        $('#add-btn').on('click', async () => await this.openCreatePage());
        $('#audit-btn').on('click', async () => await this.openAudit());
        $('#delete-btn').on('click', async () => await this.openDelete());
        $('#print-btn').on('click', async () => await this.openPrint());
        $('#printSeperatePrintReport-btn').on('click', async () => await this.openPrintSeperatePrintReport());
        $('#detail-btn').on('click', async () => await this.doDetail());
        this.initTraceAutoComplete($("[name='productName']"), function (query, done) {
            categoryController.lookupCategories(query, done);
        });
        this.initTraceAutoComplete($("[name='originName']"), function (query, done) {
            cityController.lookupCities(query, done);
        });
        this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());
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
            var resp = await jq.postJson(this.toUrl('/ecommerceBill/queryEvents.action'), billIdList);
            resp.forEach(btnid => { $('#' + btnid).show(); });
        }
        catch (e) {
            console.error(e);
        }
    }
    async openAudit() {
        var rows = this.grid.bootstrapTable("getSelections");
        if (rows.length == 0) {
            bs4pop.alert("请选择一条数据", { type: "warning" });
            return;
        }
        if (rows.length > 1) {
            bs4pop.alert("请选择数据过多", { type: "warning" });
            return;
        }
        var rowsArray = $.makeArray(rows);
        var waitAuditRowArray = rowsArray.filter(function (v, i) {
            return v.verifyStatus == 0;
        });
        if (waitAuditRowArray.length != 1) {
            bs4pop.alert("请选择待审核数据", { type: "warning" });
            return;
        }
        var hasOriginCertifiyUrlRowArray = waitAuditRowArray.filter(function (v, i) {
            return $.trim(v.originCertifiyUrl) != '';
        });
        var hasDetectReportUrlRowArray = waitAuditRowArray.filter(function (v, i) {
            return $.trim(v.detectReportUrl) != '';
        });
        var reconfirmPromise = new Promise((resolve, reject) => {
            resolve('');
        });
        if (hasDetectReportUrlRowArray.length == 1) {
            reconfirmPromise = new Promise((resolve, reject) => {
                bs4pop.confirm('登记单:<br/>' + waitAuditRowArray[0].code, {
                    type: 'warning', btns: [
                        {
                            label: '合格', className: 'btn-primary', onClick(cb) {
                                resolve({
                                    id: waitAuditRowArray[0].id,
                                    verifyStatus: 20,
                                    detectState: 1,
                                    detectStatus: 50
                                });
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
        }
        else {
            if (hasOriginCertifiyUrlRowArray.length == 1) {
                reconfirmPromise = new Promise((resolve, reject) => {
                    bs4pop.confirm('登记单:<br/>' + waitAuditRowArray[0].code, {
                        type: 'warning', btns: [
                            {
                                label: '检测', className: 'btn-primary', onClick(cb) {
                                    resolve({
                                        id: waitAuditRowArray[0].id,
                                        verifyStatus: 20,
                                        detectStatus: 30
                                    });
                                }
                            },
                            {
                                label: '合格不检测', className: 'btn-primary', onClick(cb) {
                                    resolve({
                                        id: waitAuditRowArray[0].id,
                                        verifyStatus: 20,
                                        detectState: 1,
                                        detectStatus: 50
                                    });
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
            }
            else {
                reconfirmPromise = new Promise((resolve, reject) => {
                    bs4pop.confirm('登记单:<br/>' + waitAuditRowArray[0].code, {
                        type: 'warning',
                        btns: [
                            {
                                label: '检测', className: 'btn-primary', onClick(cb) {
                                    resolve({
                                        id: waitAuditRowArray[0].id,
                                        verifyStatus: 20,
                                        detectStatus: 30,
                                    });
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
            }
        }
        var ret = await reconfirmPromise;
        if ($.type(ret) == 'object') {
            var result = {};
            $.ajax({
                type: "POST",
                url: '/ecommerceBill/doAudit.action',
                data: JSON.stringify(ret),
                processData: true,
                dataType: "json",
                async: false,
                contentType: "application/json; charset=utf-8",
                success: function (ret) {
                    result = ret;
                },
                error: function () {
                    result = { "code": "5000", result: "远程访问失败" };
                }
            });
            if (result.code == '200') {
                console.log(result);
                bs4pop.alert("操作成功", { type: 'success' }, function () {
                    window['ECommerceBillGrid'].removeAllAndLoadData();
                });
            }
            else {
                bs4pop.alert("操作" + result.result, { type: 'info' });
            }
        }
    }
    removeAllAndLoadData() {
        bs4pop.removeAll();
        $("body").removeClass("modal-open");
        (async () => {
            await this.queryGridData();
        })();
    }
    openCreatePage() {
        let url = this.toUrl("/ecommerceBill/create.html");
        var dia = bs4pop.dialog({
            title: '创建登记单',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '90%',
            height: '78%',
            btns: [],
            onShowEnd: function () {
            }
        });
    }
    async findHighLightBill() {
        try {
            var url = this.toUrl("/ecommerceBill/findHighLightBill.action");
            return await jq.postJson(url, {}, {});
        }
        catch (e) {
            console.log(e);
            return {};
        }
    }
    async openDelete() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            bs4pop.alert("请选择一条数据", { type: 'warning' });
            return;
        }
        var rowsArray = $.makeArray(row);
        var waitAuditRowArray = rowsArray.filter(function (v, i) {
            return v.verifyStatus == 0;
        });
        if (waitAuditRowArray.length != 1) {
            bs4pop.alert("单据状态不是待审核", { type: 'warning' });
            return;
        }
        let confirmPromise = new Promise((resolve, reject) => {
            bs4pop.confirm('确定要撤销登记单:' + waitAuditRowArray[0].code + " ?", {
                type: 'warning', btns: [
                    {
                        label: '撤销', className: 'btn-primary', onClick(cb) {
                            resolve(waitAuditRowArray[0].id);
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
        var ret = await confirmPromise;
        if (ret == 'cancel') {
            return;
        }
        let url = this.toUrl("/ecommerceBill/doDelete.action");
        try {
            let param = { id: ret };
            var resp = await jq.ajaxWithProcessing({
                type: "POST",
                data: JSON.stringify(param),
                url: url,
                processData: true,
                contentType: "application/json; charset=utf-8",
                dataType: "json"
            });
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
    async openPrint() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            bs4pop.alert("请选择一条数据", { type: 'warning' });
            return;
        }
        if (row.length > 1) {
            bs4pop.alert("请选择数据过多", { type: 'warning' });
            return;
        }
        let url = this.toUrl('/ecommerceBill/prePrint.action');
        try {
            let resp = jq.syncPostJson(url, { id: row[0].id });
            if (!resp.success) {
                bs4pop.alert(resp.message, { type: 'error' });
                return;
            }
            if (typeof (callbackObj) != 'undefined' && callbackObj.printDirect) {
                callbackObj.boothPrintPreview(JSON.stringify(resp.data), "StickerDocument");
            }
            else {
                bs4pop.alert("请升级客户端或者在客户端环境运行当前程序", { type: 'error' });
            }
        }
        catch (e) {
            console.error(e);
            debugger;
            bs4pop.alert('远程访问失败', { type: 'error' });
            return;
        }
    }
    async openPrintSeperatePrintReport() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            bs4pop.alert("请选择一条数据", { type: 'warning' });
            return;
        }
        if (row.length > 1) {
            bs4pop.alert("请选择数据过多", { type: 'warning' });
            return;
        }
        let url = this.toUrl("/ecommerceBill/prePrintSeperatePrintReport.html?billId=" + row[0].id);
        var dia = bs4pop.dialog({
            title: '打印分销记录',
            content: url,
            isIframe: true,
            closeBtn: true,
            backdrop: 'static',
            width: '90%',
            height: '78%',
            btns: [],
            onShowEnd: function () {
            }
        });
    }
    async doDetail() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            bs4pop.alert("请选择一条数据", { type: 'warning' });
            return;
        }
        if (row.length > 1) {
            bs4pop.alert("请选择数据过多", { type: 'warning' });
            return;
        }
        let selected_id = row[0].id;
        let url = this.toUrl('/ecommerceBill/view.html?id=' + selected_id);
        var dia = bs4pop.dialog({
            title: '查看电商登记单',
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
}
