class FieldConfigBill extends WebConfig {
    constructor(saveForm, saveBtn) {
        super();
        this.saveForm = saveForm;
        this.saveBtn = saveBtn;
        this.saveBtn.on('click', async () => this.save());
        this.saveForm.find('select[multiple]').select2({
            placeholder: '-- 可用值 --'
        });
        this.initImageCertList();
        this.initTruckType();
        this.initMeasureType();
        this.initPlate();
    }
    async initImageCertList() {
        let imageCertListDiv = this.saveForm.find('#imageCertListDiv');
        let imageCertListDisplayed = imageCertListDiv.find('[name*="displayed"]');
        let imageCertListAvailableValues = imageCertListDiv.find('select[name*="availableValues"]');
        imageCertListDisplayed.on('click', function () {
            let isDisplayed = $(this).is(":checked");
            if (isDisplayed == true) {
                imageCertListAvailableValues.attr('required', 'required');
            }
            else {
                imageCertListAvailableValues.removeAttr('required');
            }
        });
    }
    async initTruckType() {
        let truckTypeDiv = this.saveForm.find('#truckTypeDiv');
        let truckTypeDisplayed = truckTypeDiv.find('[name*="displayed"]');
        let truckTypeAvailableValues = truckTypeDiv.find('select[name*="availableValues"]');
        truckTypeDisplayed.on('click', function () {
            let isDisplayed = $(this).is(":checked");
            if (isDisplayed == true) {
                truckTypeAvailableValues.attr('required', 'required');
            }
            else {
                truckTypeAvailableValues.removeAttr('required');
                truckTypeAvailableValues.valid();
            }
        });
    }
    async initPlate() {
    }
    async initMeasureType() {
        let measureTypeDiv = this.saveForm.find('#measureTypeDiv');
        let measureTypeDisplayed = measureTypeDiv.find('[name*="displayed"]');
        let measureTypeAvailableValues = measureTypeDiv.find('select[name*="availableValues"]');
        measureTypeDisplayed.on('click', function () {
            let isDisplayed = $(this).is(":checked");
            if (isDisplayed == true) {
                measureTypeAvailableValues.attr('required', 'required');
            }
            else {
                measureTypeAvailableValues.removeAttr('required');
                measureTypeAvailableValues.valid();
            }
        });
    }
    async save() {
        if (this.saveForm.validate().form() != true) {
            return;
        }
        let data = $('#saveForm').serializeJSON({ useIntKeysAsArrayIndex: true });
        let url = this.toUrl("/fieldConfig/doUpdate.action");
        try {
            let resp = await jq.postJsonWithProcessing(url, data);
            if (!resp.success) {
                bs4pop.alert(resp.message, { type: 'error' });
                return;
            }
            bs4pop.alert('操作成功', { type: 'info', autoClose: 600 });
        }
        catch (e) {
            debugger;
            bs4pop.alert('远程访问失败', { type: 'error' });
        }
    }
}
