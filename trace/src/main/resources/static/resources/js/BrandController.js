class BrandController extends WebConfig {
    async listBrands(brandQuery) {
        var url = await super.toUrl("/brand/listBrands.action");
        let resp = await jq.postJson(url, brandQuery);
        if (resp.code == '200') {
            return resp.data;
        }
        else {
            throw new Error(resp.message);
        }
    }
    async listSuggestionsUpStreams(brandQuery) {
        try {
            let data = await this.listBrands(brandQuery);
            return _.chain(data).map(item => {
                return { "id": item.id, "value": item.name, "item": item };
            }).value();
        }
        catch (e) {
            console.error(e);
            return [];
        }
    }
    lookupBrands(likeBrandName, done) {
        (async () => {
            let data = await this.listSuggestionsUpStreams({ 'likeBrandName': likeBrandName });
            var result = { 'suggestions': data };
            done(result);
        })();
    }
}
