class jq {
    static syncPostJson(url, data, settings = {}) {
        let jsonData = jq.removeEmptyProperty(data);
        _.extend(settings, {
            method: 'post',
            dataType: 'json',
            async: false,
            contentType: 'application/json',
            data: JSON.stringify(jsonData),
            url: url
        });
        var ret = null;
        var ex = null;
        $.ajax({
            url: url,
            method: 'post',
            contentType: 'application/json',
            async: false,
            data: JSON.stringify(jsonData),
            success: function (resp) {
                ret = resp;
            },
            error: function (e) {
                ex = e;
            }
        });
        if (ex != null) {
            throw ex;
        }
        return ret;
    }
    static async ajax(settings, beforeStartFun = async () => { }, alwaysFun = async () => { }) {
        (async () => {
            await beforeStartFun();
        })();
        let p = new Promise((resolve, reject) => {
            $.ajax(settings).done((result) => {
                resolve(result);
            }).fail((e) => {
                reject(e);
            }).always(() => {
                (async () => {
                    await alwaysFun();
                })();
            });
        });
        let resp = await p;
        return resp;
    }
    static defaultBeforeStart() {
        return new Promise((resolve, reject) => {
            bui.loading.show('努力提交中，请稍候。。。');
            resolve(true);
        });
    }
    static defaultAlwaysFun() {
        return new Promise((resolve, reject) => {
            bui.loading.hide();
            resolve(true);
        });
    }
    static async ajaxWithProcessing(settings) {
        let resp = await jq.ajax(settings, jq.defaultBeforeStart, jq.defaultAlwaysFun);
        return resp;
    }
    static async postJson(url, data, settings = {}) {
        let jsonData = this.removeEmptyProperty(data);
        _.extend(settings, {
            method: 'post',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(jsonData),
            url: url
        });
        let resp = await jq.ajax(settings);
        return resp;
    }
    static async getWithProcessing(url, settings = {}) {
        _.extend(settings, { method: 'get', url: url });
        return await this.ajaxWithProcessing(settings);
    }
    static async postJsonWithProcessing(url, data, settings = {}) {
        let jsonData = this.removeEmptyProperty(data);
        _.extend(settings, {
            method: 'post',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(jsonData),
            url: url
        });
        return await this.ajaxWithProcessing(settings);
    }
    static removeEmptyProperty(data) {
        if (_.isArray(data)) {
            let jsonData = _.chain(data).filter(item => !_.isNull(item)).value();
            return jsonData;
        }
        else {
            let jsonData = _.chain(data)
                .pick((v, k) => {
                return !_.isUndefined(v);
            })
                .pick((v, k) => {
                if (_.isString(v)) {
                    if (_.isEmpty(v) || _.isNull(v)) {
                        return false;
                    }
                }
                return true;
            })
                .mapObject(v => {
                if (!_.isArray(v)) {
                    return v;
                }
                return _.chain(v).filter(v => !_.isNull(v)).value();
            })
                .value();
            return jsonData;
        }
    }
}
class popwrapper {
    static async confirm(msg, config = {}) {
        const p = new Promise((resolve, reject) => {
            bs4pop.confirm(msg, config, async function (sure) {
                resolve(sure);
            });
        });
        return await p;
    }
    static async alert(msg, config = {}) {
        const p = new Promise((resolve, reject) => {
            var alertDialog = bs4pop.alert(msg, config, function () {
                resolve(1);
            });
            if (config.autoClose) {
                setTimeout(function () {
                    alertDialog.$el.siblings('.modal-backdrop').remove();
                    alertDialog.$el.remove();
                    resolve(2);
                }, parseInt(config.autoClose));
            }
            else {
                resolve(3);
            }
        });
        return await p;
    }
}
class p {
    static call(funName, args = []) {
        p.multi('call', [{ funName: funName, args: args }]);
    }
    static exec(fun) {
        let data = { 'type': 'exec', 'fun': fun.toString() };
        window.parent.postMessage(JSON.stringify(data));
    }
    static applies(calls) {
        p.multi('call', calls);
    }
    static multi(type, calls) {
        let data = { 'type': type, 'calls': calls };
        window.parent.postMessage(JSON.stringify(data));
    }
}
