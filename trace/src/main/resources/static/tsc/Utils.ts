class jq {
    public static syncPostJson(url: string, data: any, settings: JQuery.AjaxSettings = {}) {
        let jsonData = jq.removeEmptyProperty(data);
        _.extend(settings, {
            method: 'post',
            dataType: 'json',
            async: false,
            contentType: 'application/json',
            data: JSON.stringify(jsonData),
            url: url
        })
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

    /**
     * 对ajax异步请求转换为promise
     * @param settings
     */
    public static async ajax(settings: JQuery.AjaxSettings,
                             beforeStartFun: Function = async () => {},
                             alwaysFun: Function = async () => {},): Promise<any> {
        (async () => {
            await beforeStartFun();
        })();

        let p = new Promise<any>((resolve, reject) => {
            $.ajax(settings).done((result) => {
                resolve(result);
            }).fail((e) => {
                reject(e)
            }).always(() => {
                (async () => {
                    await alwaysFun();
                })();
            })
        })
        let resp = await p;
        return resp
    }

    private static defaultBeforeStart(): Promise<any> {
        return new Promise<any>((resolve, reject) => {
                //@ts-ignore
                bui.loading.show('努力提交中，请稍候。。。');
                resolve(true);
            }
        );
    }

    private static defaultAlwaysFun(): Promise<any> {
        return new Promise<any>((resolve, reject) => {
            //@ts-ignore
            //bs4pop.removeAll();
            //@ts-ignore
            bui.loading.hide();
            resolve(true);
            }
        );
    }

    /**
     * 对ajax异步请求转换为promise
     * @param settings
     */
    public static async ajaxWithProcessing(settings: JQuery.AjaxSettings): Promise<any> {
        let resp = await jq.ajax(settings, jq.defaultBeforeStart, jq.defaultAlwaysFun);
        return resp;
    }

    /**
     * 对ajax异步请求转换为promise
     * @param settings
     */

    public static async postJson(url: string, data: object, settings: JQuery.AjaxSettings = {}): Promise<any> {
        let jsonData = this.removeEmptyProperty(data);
        _.extend(settings, {
            method: 'post',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(jsonData),
            url: url
        })
        let resp: any = await jq.ajax(settings);
        return resp;
    }

    /**
     * 对ajax异步请求转换为promise
     * @param settings
     */

    public static async getWithProcessing(url: string, settings: JQuery.AjaxSettings = {}): Promise<any> {
        _.extend(settings, {method: 'get', url: url})
        return await this.ajaxWithProcessing(settings)
    }

    /**
     * 对ajax异步请求转换为promise
     * @param settings
     */

    public static async postJsonWithProcessing(url: string, data: object, settings: JQuery.AjaxSettings = {}): Promise<any> {
        let jsonData = this.removeEmptyProperty(data);
        _.extend(settings, {
            method: 'post',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(jsonData),
            url: url
        })
        return await this.ajaxWithProcessing(settings)
    }

    public static removeEmptyProperty(data: any) {
        if (_.isArray(data)) {
            let jsonData = _.chain(data).filter(item => !_.isNull(item)).value();
            return jsonData;
        } else {
            //属性值为空字符串，以及数组里面null的数据给移除掉
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
    /**
     * 对ajax异步请求转换为promise
     * @param settings
     */
    public static async confirm(msg: string, config: any = {}): Promise<any> {
        const p = new Promise<any>((resolve, reject) => {
            //@ts-ignore
            bs4pop.confirm(msg, config, async function (sure) {
                resolve(sure);
            })
        })
        return await p;
    }

    public static async alert(msg: string, config: any = {}) {

        const p = new Promise<any>((resolve, reject) => {
            //@ts-ignore
            var alertDialog = bs4pop.alert(msg, config, function () {
                resolve(1)
            });
            if (config.autoClose) {
                setTimeout(function () {
                    alertDialog.$el.siblings('.modal-backdrop').remove()
                    alertDialog.$el.remove()
                    resolve(2)
                }, parseInt(config.autoClose))
            } else {
                resolve(3)
            }

        })
        return await p;
    }
}

interface Call {
    funName: string;
    args: any[];
}

class p {
    public static call(funName: string, args: any[] = []) {
        p.multi('call', [{funName: funName, args: args}]);
    }

    public static exec(fun: Function) {
        let data = {'type': 'exec', 'fun': fun.toString()};
        // @ts-ignore
        window.parent.postMessage(JSON.stringify(data));
    }

    public static applies(calls: Call[]) {
        p.multi('call', calls);
    }

    private static multi(type: string, calls: Call[]) {
        let data = {'type': type, 'calls': calls};
        // @ts-ignore
        window.parent.postMessage(JSON.stringify(data));
    }
}