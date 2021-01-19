(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([[2],{aa43:function(e,s,t){"use strict";t("d3a2")},cb44:function(e,s,t){},d3a2:function(e,s,t){},e7a7:function(e,s,t){"use strict";t("cb44")},f133:function(e,s,t){"use strict";t.r(s);var n=function(){var e=this,s=e.$createElement,t=e._self._c||s;return t("q-page",{staticClass:"column"},[t("div",{staticClass:"self-center text-h4",staticStyle:{"margin-top":"20px"}},[e._v("\n    "+e._s(e.id)+"\n  ")]),t("div",{staticClass:"column",staticStyle:{margin:"20px"}},[void 0!==e.sensor.description&&"no description"!==e.sensor.description?t("span",{staticClass:"text-subtitle1"},[e._v(e._s(e.sensor.description))]):t("span",{staticClass:"text-subtitle1 text-grey-5"},[e._v("no description")]),void 0!==e.sensor.unit&&"no unit"!==e.sensor.unit?t("span",{staticClass:"text-subtitle2"},[e._v(e._s(e.sensor.unit))]):t("span",{staticClass:"text-subtitle2 text-grey-5"},[e._v("no unit")]),void 0!==e.sensor.category&&"other"!==e.sensor.category?t("span",{staticClass:"text-grey-5"},[t("span",[e._v("category ")]),t("span",[e._v(" "+e._s(e.sensor.category))])]):e._e(),void 0!==e.sensor.visibility&&"public"!==e.sensor.visibility?t("span",{staticClass:"text-grey-5"},[t("span",[e._v("visibility ")]),t("span",[e._v(" "+e._s(e.sensor.visibility))])]):e._e(),void 0!==e.sensor.derived&&e.sensor.derived?t("span",[t("span",{staticClass:"text-grey-5"},[e._v("derived sensor")])]):e._e(),void 0!==e.sensor.dependency?t("span",{staticClass:"text-subtitle1"},[t("span",[e._v("dependency ")]),t("span",{staticClass:"text-weight-medium"},e._l(e.sensor.dependency,(function(s){return t("a",{key:s,staticStyle:{"margin-left":"10px"},attrs:{href:"#/model/sensors/"+s}},[e._v(e._s(s))])})),0)]):e._e()]),t("div",{staticClass:"column processing-node"},[t("span",[t("span",{staticClass:"text-h6"},[e._v("Raw")]),t("span",{staticClass:"text-subtitle1"},[void 0===e.sensor.raw_source?t("span",{staticClass:"text-grey-5"},[e._v(" from database ")]):e._e(),void 0!==e.sensor.raw_source?t("span",{staticClass:"text-grey-5"},[e._v(" from ")]):e._e(),void 0!==e.sensor.raw_source?t("span",{staticClass:"text-weight-medium"},e._l(e.sensor.raw_source,(function(s){return t("a",{key:s,staticStyle:{"margin-left":"10px"},attrs:{href:"#/model/sensors/"+s}},[e._v(e._s(s))])})),0):e._e()])]),t("div",{staticClass:"property-grid"},[void 0!==e.sensor.physical_range?[t("div",[e._v("physical_range")]),t("div",[e._v(e._s(e.sensor.physical_range[0])+" .. "+e._s(e.sensor.physical_range[1]))])]:e._e(),void 0!==e.sensor.step_range?[t("div",[e._v("step_range")]),t("div",[e._v(e._s(e.sensor.step_range[0])+" .. "+e._s(e.sensor.step_range[1]))])]:e._e(),void 0!==e.sensor.raw_func?[t("div",[e._v("raw_func")]),t("div",[e._v(e._s(e.sensor.raw_func))]),void 0!==e.sensor.raw_func_parsed?[t("div",[e._v("--\x3e parsed")]),t("div",[e._v(e._s(e.sensor.raw_func_parsed))]),t("div",[e._v("--\x3e print")]),t("formula-print",{attrs:{node:e.sensor.raw_func_print,level:0}})]:e._e()]:e._e()],2)]),"none"!==e.sensor.aggregation_hour?t("div",{staticClass:"column processing-node"},[t("span",[t("span",{staticClass:"text-h6"},[e._v("Hour")]),t("span",{staticClass:"text-subtitle1"},[t("span",{staticClass:"text-grey-5"},[e._v(" from raw by ")]),t("span",{staticClass:"text-weight-medium"},[e._v(" "+e._s(e.sensor.aggregation_hour))])])]),t("div",{staticClass:"property-grid"},[void 0!==e.sensor.empirical_diff?[t("div",[e._v("empirical_diff_range")]),t("div",[e._v(e._s(e.sensor.empirical_diff))])]:e._e(),void 0!==e.sensor.interpolation_mse?[t("div",[e._v("interpolation_mse")]),t("div",[e._v(e._s(e.sensor.interpolation_mse))])]:e._e(),void 0!==e.sensor.post_hour_func?[t("div",[e._v("post_hour_func")]),t("div",[e._v(e._s(e.sensor.post_hour_func))]),void 0!==e.sensor.post_hour_func_parsed?[t("div",[e._v("--\x3e parsed")]),t("div",[e._v(e._s(e.sensor.post_hour_func_parsed))]),t("div",[e._v("--\x3e print")]),t("formula-print",{attrs:{node:e.sensor.post_hour_func_print,level:0}})]:e._e()]:e._e()],2)]):e._e(),"none"===e.sensor.aggregation_hour?t("div",{staticClass:"text-grey-5",staticStyle:{margin:"20px"}},[e._v("\n    (no further aggregation defined)\n  ")]):e._e(),"none"!==e.sensor.aggregation_hour&&"none"!==e.sensor.aggregation_day?t("div",{staticClass:"column processing-node"},[t("span",[t("span",{staticClass:"text-h6"},[e._v("Day")]),t("span",{staticClass:"text-subtitle1"},[t("span",{staticClass:"text-grey-5"},[e._v(" from hour by ")]),t("span",{staticClass:"text-weight-medium"},[e._v(" "+e._s(e.sensor.aggregation_day))])])]),t("div",{staticClass:"property-grid"},[void 0!==e.sensor.post_day_func?[t("div",[e._v("post_day_func")]),t("div",[e._v(e._s(e.sensor.post_day_func))]),void 0!==e.sensor.post_day_func_parsed?[t("div",[e._v("--\x3e parsed")]),t("div",[e._v(e._s(e.sensor.post_day_func_parsed))]),t("div",[e._v("--\x3e print")]),t("formula-print",{attrs:{node:e.sensor.post_day_func_print,level:0}})]:e._e()]:e._e()],2)]):e._e(),"none"!==e.sensor.aggregation_hour&&"none"===e.sensor.aggregation_day?t("div",{staticClass:"text-grey-5",staticStyle:{margin:"20px"}},[e._v("\n    (no further aggregation defined)\n  ")]):e._e(),"none"!==e.sensor.aggregation_hour&&"none"!==e.sensor.aggregation_day&&"none"!==e.sensor.aggregation_week?t("div",{staticClass:"column processing-node"},[t("span",[t("span",{staticClass:"text-h6"},[e._v("Week")]),t("span",{staticClass:"text-subtitle1"},[t("span",{staticClass:"text-grey-5"},[e._v(" from day by ")]),t("span",{staticClass:"text-weight-medium"},[e._v(" "+e._s(e.sensor.aggregation_week))])])]),t("div",{staticClass:"property-grid"})]):e._e(),"none"!==e.sensor.aggregation_hour&&"none"!==e.sensor.aggregation_day&&"none"===e.sensor.aggregation_week?t("div",{staticClass:"text-grey-5",staticStyle:{margin:"20px"}},[e._v("\n    (no further aggregation defined)\n  ")]):e._e(),"none"!==e.sensor.aggregation_hour&&"none"!==e.sensor.aggregation_day&&"none"!==e.sensor.aggregation_month?t("div",{staticClass:"column processing-node"},[t("span",[t("span",{staticClass:"text-h6"},[e._v("Month")]),t("span",{staticClass:"text-subtitle1"},[t("span",{staticClass:"text-grey-5"},[e._v(" from day by ")]),t("span",{staticClass:"text-weight-medium"},[e._v(" "+e._s(e.sensor.aggregation_month))])])]),t("div",{staticClass:"property-grid"})]):e._e(),"none"!==e.sensor.aggregation_hour&&"none"!==e.sensor.aggregation_day&&"none"===e.sensor.aggregation_month?t("div",{staticClass:"text-grey-5",staticStyle:{margin:"20px"}},[e._v("\n    (no further aggregation defined)\n  ")]):e._e(),"none"!==e.sensor.aggregation_hour&&"none"!==e.sensor.aggregation_day&&"none"!==e.sensor.aggregation_month&&"none"!==e.sensor.aggregation_year?t("div",{staticClass:"column processing-node"},[t("span",[t("span",{staticClass:"text-h6"},[e._v("Year")]),t("span",{staticClass:"text-subtitle1"},[t("span",{staticClass:"text-grey-5"},[e._v(" from month by ")]),t("span",{staticClass:"text-weight-medium"},[e._v(" "+e._s(e.sensor.aggregation_year))])]),t("div",{staticClass:"property-grid"})])]):e._e(),"none"!==e.sensor.aggregation_hour&&"none"!==e.sensor.aggregation_day&&"none"!==e.sensor.aggregation_month&&"none"===e.sensor.aggregation_year?t("div",{staticClass:"text-grey-5",staticStyle:{margin:"20px"}},[e._v("\n    (no further aggregation defined)\n  ")]):e._e()])},o=[],a=(t("e6cf"),t("ded3")),r=t.n(a),i=t("2f62"),l=function(){var e=this,s=e.$createElement,t=e._self._c||s;return t("div",{class:[e.formulaOpClass,e.levelClass]},["add"===e.node.op?[e._l(e.node.terms,(function(s,n){return[t("div",{key:JSON.stringify(s)+0,class:{"formula-op-add-column":!0,"formula-op-add-column-follow":n>0}},[e._v(e._s(0==n&&s.positive?"":s.positive?"+":"-"))]),t("formula-print",{key:JSON.stringify(s)+1,attrs:{node:s.term,level:1===e.node.depth?e.level:e.level+1}})]}))]:"var"===e.node.op?[e._v("\n    "+e._s(e.node.name)+" \n  ")]:"const"===e.node.op?[e._v("\n    "+e._s(e.node.value)+" \n  ")]:"div"===e.node.op?[t("formula-print",{attrs:{node:e.node.a,level:1===e.node.depth?e.level:e.level+1}}),t("div",{staticClass:"formula-op-div-line"}),t("formula-print",{attrs:{node:e.node.b,level:1===e.node.depth?e.level:e.level+1}})]:"if"===e.node.op?[t("div",{staticClass:"formula-op-if-ifthen"},[e._v("IF")]),t("formula-print",{attrs:{node:e.node.p,level:1===e.node.depth?e.level:e.level+1}}),t("div",{staticClass:"formula-op-if-ifthen"},[e._v("THEN")]),t("div",{staticClass:"formula-op-if-block"},[t("formula-print",{attrs:{node:e.node.a,level:1===e.node.depth?e.level:e.level+1}}),t("div",{staticClass:"formula-op-if-else"},[e._v("ELSE")]),t("formula-print",{attrs:{node:e.node.b,level:1===e.node.depth?e.level:e.level+1}})],1)]:"rel"===e.node.pred_op?[t("formula-print",{attrs:{node:e.node.a,level:1===e.node.depth?e.level:e.level+1}}),"="===e.node.name?t("div",[e._v("=")]):"<"===e.node.name?t("div",[e._v("<")]):"<="===e.node.name?t("div",[e._v("≤")]):"!="===e.node.name?t("div",[e._v("≠")]):t("div",[e._v(e._s(e.node.name))]),t("formula-print",{attrs:{node:e.node.b,level:1===e.node.depth?e.level:e.level+1}})]:"mul"===e.node.op?[e._l(e.node.factors,(function(s,n){return[n>0?t("div",{key:JSON.stringify(s)+n},[e._v("·")]):e._e(),t("formula-print",{key:JSON.stringify(s),attrs:{node:s,level:1===e.node.depth?e.level:e.level+1}})]}))]:"pow"===e.node.op?[t("formula-print",{attrs:{node:e.node.a,level:1===e.node.depth?e.level:e.level+1}}),t("div",[e._v("^")]),t("formula-print",{staticClass:"formula-op-pow-exp",attrs:{node:e.node.b,level:1===e.node.depth?e.level:e.level+1}})]:"and"===e.node.pred_op?[e._l(e.node.preds,(function(s,n){return[t("div",{key:JSON.stringify(s)+n,staticClass:"formula-op-and-column"},[e._v(e._s(0==n?"":"AND"))]),t("formula-print",{key:JSON.stringify(s),attrs:{node:s,level:1===e.node.depth?e.level:e.level+1}})]}))]:"func"===e.node.op?[t("div",[e._v(e._s(e.node.name)+"(")]),t("formula-print",{attrs:{node:e.node.param,level:e.level}}),t("div",[e._v(")")])]:"or"===e.node.pred_op?[e._l(e.node.preds,(function(s,n){return[n>0?t("div",{key:JSON.stringify(s)+n},[e._v("OR")]):e._e(),t("formula-print",{key:JSON.stringify(s),attrs:{node:s,level:1===e.node.depth?e.level:e.level+1}})]}))]:"const"===e.node.pred_op?[e._v("\n    "+e._s(e.node.value)+"\n  ")]:[e._v("\n    "+e._s(e.node)+"\n  ")]],2)},d=[],_={name:"formula-print",props:["node","level"],data(){return{maxLevel:7}},computed:{levelClass(){return"level"+(this.level<=this.maxLevel?this.level:this.maxLevel)},formulaOpClass(){switch(this.node.op){case"add":return"formula-op-add";case"div":return"formula-op-div";case"if":return"formula-op-if";case"rel":return"formula-op-rel";case"mul":return"formula-op-mul";case"pow":return"formula-op-pow";case"func":return"formula-op-func";default:switch(this.node.pred_op){case"rel":return"formula-op-rel";case"and":return"formula-op-and";case"or":return"formula-op-or";default:return""}}}}},v=_,p=(t("aa43"),t("2877")),c=Object(p["a"])(v,l,d,!1,null,"48cd5fcc",null),u=c.exports,g={name:"sensor",components:{formulaPrint:u},props:["id"],computed:r()(r()({},Object(i["b"])({model:e=>e.model.data})),{},{sensor(){return void 0===this.model||void 0===this.id||void 0===this.model.sensors[this.id]?{}:this.model.sensors[this.id]}}),async mounted(){this.$store.dispatch("model/init")}},f=g,m=(t("e7a7"),t("9989")),y=t("eebe"),h=t.n(y),C=Object(p["a"])(f,n,o,!1,null,"b1b78544",null);s["default"]=C.exports;h()(C,"components",{QPage:m["a"]})}}]);