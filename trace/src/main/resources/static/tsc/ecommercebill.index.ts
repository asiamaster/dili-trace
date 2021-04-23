class EcommerceBillGrid extends ListPage {
    grid: any;
    queryform: any;
    highLightBill: any;
    btns:any[];
    toolbar:any;

    constructor(grid: any, queryform: any, toolbar: any) {
        super(grid,queryform,queryform.find('#query'),"/ecommerceBill/listPage.action");
        this.grid = grid;
        this.queryform = queryform;
        this.toolbar=toolbar;
        let categoryController: CategoryController = new CategoryController();
        let cityController: CityController = new CityController();
        window['ECommerceBillGrid'] = this;
        this.btns=this.toolbar.find('button');
        $('#add-btn').on('click', async () => await this.openCreatePage());
        $('#audit-btn').on('click', async () => await this.openAudit());
        $('#delete-btn').on('click', async () => await this.openDelete());
        $('#print-btn').on('click', async () => await this.openPrint());
        $('#printSeperatePrintReport-btn').on('click', async () => await this.openPrintSeperatePrintReport());
        $('#detail-btn').on('click', async () => await this.doDetail());

        this.initTraceAutoComplete($("[name='productName']"), function (query, done) {
            categoryController.lookupCategories(query, done)
        });
        this.initTraceAutoComplete($("[name='originName']"), function (query, done) {
            cityController.lookupCities(query, done)
        });
        this.grid.on('check.bs.table uncheck.bs.table', async () => await this.resetButtons());

        /*this.queryform.find('#query').click(async () => await this.queryGridData());
        //load data
        (async () => {
            await this.queryGridData();
        })();*/
    }
    protected async resetButtons() {
        var btnArray=this.btns;
        _.chain(btnArray).each((btn)=> {
            $(btn).hide();
        });
        await this.queryEventAndSetBtn();

    }
    private async queryEventAndSetBtn(){
        var rows=this.rows;
        try{
            //@ts-ignore
            var billIdList=_.chain(rows).map(v=>v.id).value();
            var resp=await jq.postJson(this.toUrl('/ecommerceBill/queryEvents.action'),billIdList);
            // console.info(resp)
            resp.forEach(btnid=>{ $('#'+btnid).show();})
        }catch (e){
            console.error(e);
        }

    }
    private async openAudit() {
        var rows = this.grid.bootstrapTable("getSelections");
        if (rows.length == 0) {
            //@ts-ignore
            bs4pop.alert("请选择一条数据", {type: "warning"});
            return;
        }
        if (rows.length > 1) {
            //@ts-ignore
            bs4pop.alert("请选择数据过多", {type: "warning"});
            return;
        }
        var rowsArray = $.makeArray(rows);
        var waitAuditRowArray = rowsArray.filter(function (v, i) {
            //@ts-ignore
            return v.verifyStatus == BillVerifyStatusEnum.WAIT_AUDIT;
        });

        if (waitAuditRowArray.length != 1) {
            //@ts-ignore
            bs4pop.alert("请选择待审核数据", {type: "warning"});
            return;
        }
        var hasOriginCertifiyUrlRowArray = waitAuditRowArray.filter(function (v, i) {
            // @ts-ignore
            return $.trim(v.originCertifiyUrl) != '';
        });

        var hasDetectReportUrlRowArray = waitAuditRowArray.filter(function (v, i) {
            // @ts-ignore
            return $.trim(v.detectReportUrl) != '';
        });
        var reconfirmPromise = new Promise((resolve, reject) => {
            resolve('');
        });

        if (hasDetectReportUrlRowArray.length == 1) {
            reconfirmPromise = new Promise((resolve, reject) => {
                // @ts-ignore
                bs4pop.confirm('登记单:<br/>' + waitAuditRowArray[0].code,
                    {
                        type: 'warning', btns: [
                            {
                                label: '合格', className: 'btn-primary', onClick(cb) {
                                    resolve({
                                        //@ts-ignore
                                        id: waitAuditRowArray[0].id,
                                        verifyStatus: BillVerifyStatusEnum.PASSED,
                                        detectState: DetectResultEnum.PASSED,
                                        detectStatus: DetectStatusEnum.FINISH_DETECT
                                    })
                                }
                            },
                            {
                                label: '取消', className: 'btn-cancel', onClick(cb) {
                                    resolve("cancel")
                                }
                            },
                        ]
                    });
            });

        } else {
            // @ts-ignore
            if (hasOriginCertifiyUrlRowArray.length == 1) {
                // @ts-ignore
                reconfirmPromise = new Promise((resolve, reject) => {
                    // @ts-ignore
                    bs4pop.confirm('登记单:<br/>' + waitAuditRowArray[0].code,
                        {
                            type: 'warning', btns: [
                                {
                                    label: '检测', className: 'btn-primary', onClick(cb) {
                                        resolve({
                                            // @ts-ignore
                                            id: waitAuditRowArray[0].id,
                                            verifyStatus: BillVerifyStatusEnum.PASSED,
                                            detectStatus: DetectStatusEnum.WAIT_DETECT
                                        });
                                    }
                                },
                                {
                                    label: '合格不检测', className: 'btn-primary', onClick(cb) {
                                        resolve({
                                            //@ts-ignore
                                            id: waitAuditRowArray[0].id,
                                            verifyStatus:BillVerifyStatusEnum.PASSED,
                                            detectState: DetectResultEnum.PASSED,
                                            detectStatus: DetectStatusEnum.FINISH_DETECT
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
            } else {
                // @ts-ignore
                reconfirmPromise = new Promise((resolve, reject) => {
                    // @ts-ignore
                    bs4pop.confirm('登记单:<br/>' + waitAuditRowArray[0].code, {
                        type: 'warning',
                        btns: [
                            {
                                label: '检测', className: 'btn-primary', onClick(cb) {
                                    resolve({
                                        // @ts-ignore
                                        id: waitAuditRowArray[0].id,
                                        verifyStatus: BillVerifyStatusEnum.PASSED,
                                        detectStatus: DetectStatusEnum.WAIT_DETECT,
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
        // @ts-ignore
        var ret = await reconfirmPromise;
        if ($.type(ret) == 'object') {
            //console.info(ret)
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
                    result = {"code": "5000", result: "远程访问失败"}

                }
            });
            // @ts-ignore
            if (result.code == '200') {
                console.log(result);
                // @ts-ignore
                bs4pop.alert("操作成功", {type: 'success'}, function () {
                        window['ECommerceBillGrid'].removeAllAndLoadData();
                    }
                );


            } else {
                // @ts-ignore
                bs4pop.alert("操作" + result.result, {type: 'info'});
            }


        }
    }

    public removeAllAndLoadData() {
        //@ts-ignore
        bs4pop.removeAll();
        //@ts-ignore
        $("body").removeClass("modal-open");
        (async () => {
            await this.queryGridData();
        })();
    }

    private openCreatePage() {
        let url = this.toUrl("/ecommerceBill/create.html");
        //@ts-ignore
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
                //dia.$el.find('iframe')[0].contentWindow['RegisterBillGridObj']=cthis;
            }
        });
    }


    private async findHighLightBill() {
        try {
            var url = this.toUrl("/ecommerceBill/findHighLightBill.action");
            return await jq.postJson(url, {}, {});
        } catch (e) {
            console.log(e);
            return {};
        }
    }

    private async openDelete() {
        let row = this.grid.bootstrapTable("getSelections");
        if (row.length == 0) {
            //@ts-ignore
            bs4pop.alert("请选择一条数据", {type: 'warning'});
            return;
        }
        var rowsArray = $.makeArray(row);
        var waitAuditRowArray = rowsArray.filter(function (v, i) {
            //@ts-ignore
            return v.verifyStatus == BillVerifyStatusEnum.WAIT_AUDIT;
        });

        if (waitAuditRowArray.length != 1) {
            //@ts-ignore
            bs4pop.alert("单据状态不是待审核", {type: 'warning'});
            return;
        }
        let confirmPromise = new Promise((resolve, reject) => {
            //@ts-ignore
            bs4pop.confirm('确定要撤销登记单:' + waitAuditRowArray[0].code + " ?", {
                type: 'warning', btns: [
                    {
                        label: '撤销', className: 'btn-primary', onClick(cb) {
                            //@ts-ignore
                            resolve(waitAuditRowArray[0].id);
                        }
                    },
                    {
                        label: '取消', className: 'btn-cancel', onClick(cb) {
                            //@ts-ignore
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
            let param = {id: ret};
            var resp = await jq.ajaxWithProcessing({
                type: "POST",
                data: JSON.stringify(param),
                url: url,
                processData: true,
                contentType: "application/json; charset=utf-8",
                dataType: "json"
            });

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

    private async openPrint() {

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

        let url=this.toUrl('/ecommerceBill/prePrint.action');

        try{
            let resp =jq.syncPostJson(url,{id: row[0].id});
            if (!resp.success) {
                //@ts-ignore
                bs4pop.alert(resp.message, {type: 'error'});
                return;
            }

            //@ts-ignore
            if (typeof (callbackObj) != 'undefined' && callbackObj.printDirect) {
                //@ts-ignore
                callbackObj.boothPrintPreview(JSON.stringify(resp.data), "StickerDocument");
            } else {
                //@ts-ignore
                bs4pop.alert("请升级客户端或者在客户端环境运行当前程序", {type: 'error'});
            }
        }catch (e){
            console.error(e)
            debugger
            //@ts-ignore
            bs4pop.alert('远程访问失败', {type: 'error'});
            return
        }

    }

    private async openPrintSeperatePrintReport() {
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

        let url = this.toUrl("/ecommerceBill/prePrintSeperatePrintReport.html?billId=" + row[0].id);
        //@ts-ignore
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
                //dia.$el.find('iframe')[0].contentWindow['RegisterBillGridObj']=cthis;
            }
        });

    }

    private async doDetail() {
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
        let selected_id = row[0].id;
        let url = this.toUrl('/ecommerceBill/view.html?id=' + selected_id);
        //@ts-ignore
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
                //dia.$el.find('iframe')[0].contentWindow['RegisterBillGridObj']=cthis;
            }
        });
    }
}
