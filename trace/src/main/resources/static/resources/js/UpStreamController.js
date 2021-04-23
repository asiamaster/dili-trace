class UpStreamController extends WebConfig {
    async listCities(upStreamQuery) {
        var url = await super.toUrl("/upStream/listByKeyWord.action");
        let resp = await jq.postJson(url, upStreamQuery);
        if (resp.code == '200') {
            return resp.data;
        }
        else {
            throw new Error(resp.message);
        }
    }
    async save(upStreamDto) {
        var url = await super.toUrl("/upStream/save.action");
        let resp = await jq.postJson(url, upStreamDto);
        if (resp.code == '200') {
            return resp.data;
        }
        else {
            throw new Error(resp.message);
        }
    }
    async listSuggestionsUpStreams(upStreamQuery) {
        try {
            let data = await this.listCities(upStreamQuery);
            return _.chain(data).map(item => {
                return { "id": item.id, "value": item.name, "item": item };
            }).value();
        }
        catch (e) {
            console.error(e);
            return [];
        }
    }
    lookupUpStreams(keyword, userId, done) {
        (async () => {
            let data = await this.listSuggestionsUpStreams({ 'keyword': keyword, userId: userId });
            var result = { 'suggestions': data };
            done(result);
        })();
    }
}
