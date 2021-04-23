
     class FieldConfigBill extends WebConfig {
        private saveForm: JQuery;
        private saveBtn: JQuery;


        constructor(saveForm: JQuery, saveBtn: JQuery) {
            super();
            this.saveForm = saveForm;
            this.saveBtn = saveBtn;
            this.saveBtn.on('click', async () => this.save());

            //@ts-ignore
            this.saveForm.find('select[multiple]').select2({
                placeholder: '-- 可用值 --'
            });
            this.initImageCertList();
            this.initTruckType();
            this.initMeasureType();
            this.initPlate();

        }

        private async initImageCertList() {
            let imageCertListDiv: JQuery = this.saveForm.find('#imageCertListDiv');
            let imageCertListDisplayed = imageCertListDiv.find('[name*="displayed"]')
            let imageCertListAvailableValues = imageCertListDiv.find('select[name*="availableValues"]')

            imageCertListDisplayed.on('click', function () {
                let isDisplayed = $(this).is(":checked");
                if (isDisplayed == true) {
                    imageCertListAvailableValues.attr('required', 'required')
                } else {
                    imageCertListAvailableValues.removeAttr('required')
                }
            });
        }

        private async initTruckType() {
            let truckTypeDiv: JQuery = this.saveForm.find('#truckTypeDiv');
            let truckTypeDisplayed = truckTypeDiv.find('[name*="displayed"]')
            let truckTypeAvailableValues = truckTypeDiv.find('select[name*="availableValues"]')

            truckTypeDisplayed.on('click', function () {
                let isDisplayed = $(this).is(":checked");
                if (isDisplayed == true) {
                    truckTypeAvailableValues.attr('required', 'required')
                } else {
                    truckTypeAvailableValues.removeAttr('required')
                    //@ts-ignore
                    truckTypeAvailableValues.valid();
                }

            });
        }

        private async initPlate() {
        }

        private async initMeasureType() {
            let measureTypeDiv: JQuery = this.saveForm.find('#measureTypeDiv');
            let measureTypeDisplayed = measureTypeDiv.find('[name*="displayed"]')
            let measureTypeAvailableValues = measureTypeDiv.find('select[name*="availableValues"]')


            measureTypeDisplayed.on('click', function () {
                let isDisplayed = $(this).is(":checked");
                if (isDisplayed == true) {
                    measureTypeAvailableValues.attr('required', 'required')
                } else {
                    measureTypeAvailableValues.removeAttr('required')
                    //@ts-ignore
                    measureTypeAvailableValues.valid();
                }

            });
        }

        private async save() {
            //@ts-ignore
            if (this.saveForm.validate().form() != true) {
                return;
            }
            //@ts-ignore
            let data = $('#saveForm').serializeJSON({useIntKeysAsArrayIndex: true});
            let url = this.toUrl("/fieldConfig/doUpdate.action");
            try {
                let resp = await jq.postJsonWithProcessing(url, data);
                if (!resp.success) {
                    //@ts-ignore
                    bs4pop.alert(resp.message, {type: 'error'});
                    return;
                }
                //@ts-ignore
                bs4pop.alert('操作成功', {type: 'info', autoClose: 600});
            } catch (e) {
                debugger
                //@ts-ignore
                bs4pop.alert('远程访问失败', {type: 'error'});
            }

        }


    }