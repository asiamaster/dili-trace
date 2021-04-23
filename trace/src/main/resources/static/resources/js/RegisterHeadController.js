class RegisterHeadController extends WebConfig {
    async listRegisterHead(registerHeadQuery) {
        var url = await super.toUrl("/registerHead/listRegisterHead.action");
        let resp = await jq.postJson(url, registerHeadQuery);
        if (resp.code == '200') {
            return resp.data;
        }
        else {
            throw new Error(resp.message);
        }
    }
    async listSuggestionsRegisterHeads(registerHeadQuery) {
        try {
            let data = await this.listRegisterHead(registerHeadQuery);
            return _.chain(data).map(item => {
                return { "id": item.id, "value": item.mergerName, "item": item };
            }).value();
        }
        catch (e) {
            console.error(e);
            return [];
        }
    }
    lookupRegisterHead(likeProductName, done) {
        (async () => {
            let data = await this.listRegisterHead({ 'likeProductName': likeProductName });
            var result = { 'suggestions': data };
            done(result);
        })();
    }
}
