(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.demo = exports.abc = exports.DemoClass = void 0;
    class DemoClass {
        constructor(props) {
            $.makeArray("");
            _.chain([]).value();
            console.info("bcd");
        }
    }
    exports.DemoClass = DemoClass;
    var abc;
    (function (abc) {
        let def;
        (function (def) {
            class A {
                constructor() {
                }
            }
            def.A = A;
            ;
            class B {
            }
            def.B = B;
            ;
        })(def = abc.def || (abc.def = {}));
    })(abc = exports.abc || (exports.abc = {}));
    var demo;
    (function (demo) {
        class Test1 {
            constructor() {
                let a = new abc.def.A();
                (async () => {
                    await a.hello();
                })();
            }
        }
        demo.Test1 = Test1;
        class Test2 {
            constructor() {
            }
        }
        demo.Test2 = Test2;
    })(demo = exports.demo || (exports.demo = {}));
    let a = { name: "abc" };
    a.age = 23;
    a.name = "ddd";
    console.info(a);
    let b = (a1, b1) => {
        return a1 > b1;
    };
    let c = ['a', 'b'];
    let d = ['a', 'b'];
    let e = [1];
    let f = [1, 2, 3];
    let g = [1, 'ss'];
    class ImplE {
        constructor() {
            this.age = 23;
            this.name = "abc";
        }
    }
    var e2 = new ImplE();
    e2.name = "ss";
    let ss = { name: '', age: 34 };
    let n = { age: 23, name: '' };
    var comp = Vue.component("demo", {
        props: ['todo'],
        template: '<a href="">{{todo.text}}</a>',
    });
    var app = new Vue({
        el: '',
        data: {},
    });
    console.info("abcd");
});
