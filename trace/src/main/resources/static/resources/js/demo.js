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
    console.info("abcd");
});
