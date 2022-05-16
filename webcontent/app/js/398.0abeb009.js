"use strict";(self["webpackChunkapp"]=self["webpackChunkapp"]||[]).push([[398],{3828:(e,s,t)=>{t.d(s,{Z:()=>v});var o=t(9835),n=t(6970);const a=e=>((0,o.dD)("data-v-9fde52e0"),e=e(),(0,o.Cn)(),e),r={class:"text-h5"},l=a((()=>(0,o._)("div",null,null,-1)));function i(e,s,t,a,i,d){const m=(0,o.up)("pages-menu"),p=(0,o.up)("q-toolbar");return(0,o.wg)(),(0,o.j4)(p,{class:"title fit row wrap justify-between"},{default:(0,o.w5)((()=>[(0,o.Wm)(m,{active:t.active},null,8,["active"]),(0,o._)("div",r,(0,n.zw)(t.title),1),l])),_:1})}function d(e,s,t,a,r,l){const i=(0,o.up)("q-item-section"),d=(0,o.up)("q-item"),m=(0,o.up)("q-list"),p=(0,o.up)("q-menu"),c=(0,o.up)("q-btn");return(0,o.wg)(),(0,o.j4)(c,{color:"primary",round:"",icon:"menu"},{default:(0,o.w5)((()=>[(0,o.Wm)(p,{"transition-show":"scale","transition-hide":"scale"},{default:(0,o.w5)((()=>[(0,o.Wm)(m,{style:{"min-width":"100px"}},{default:(0,o.w5)((()=>[((0,o.wg)(!0),(0,o.iD)(o.HY,null,(0,o.Ko)(r.items,(e=>((0,o.wg)(),(0,o.j4)(d,{key:e.title,to:e.link,clickable:"",class:(0,n.C_)({activeitem:t.active===e.link})},{default:(0,o.w5)((()=>[(0,o.Wm)(i,null,{default:(0,o.w5)((()=>[(0,o.Uk)((0,n.zw)(e.title),1)])),_:2},1024)])),_:2},1032,["to","class"])))),128))])),_:1})])),_:1})])),_:1})}const m={name:"pages-menu",props:["active"],data(){return{items:[{title:"Overview",link:"/overview"},{title:"Metadata",link:"/model"},{title:"Diagrams",link:"/diagram"},{title:"Monitoring",link:"/monitoring"}]}},computed:{},methods:{},watch:{},async mounted(){}};var p=t(1639),c=t(4455),u=t(5290),w=t(3246),h=t(490),g=t(1233),E=t(9984),k=t.n(E);const f=(0,p.Z)(m,[["render",d],["__scopeId","data-v-0bf92e70"]]),H=f;k()(m,"components",{QBtn:c.Z,QMenu:u.Z,QList:w.Z,QItem:h.Z,QItemSection:g.Z});const G={name:"pages-toolbar",props:["title","active"],components:{pagesMenu:H},data(){return{}},computed:{},methods:{},watch:{},async mounted(){}};var _=t(1663);const b=(0,p.Z)(G,[["render",i],["__scopeId","data-v-9fde52e0"]]),v=b;k()(G,"components",{QToolbar:_.Z})},4398:(e,s,t)=>{t.r(s),t.d(s,{default:()=>S});var o=t(9835),n=t(6970);const a=(0,o.Uk)("refresh"),r={key:0},l={key:1},i={key:0,style:{color:"red"}};function d(e,s,t,d,m,p){const c=(0,o.up)("pages-toolbar"),u=(0,o.up)("q-header"),w=(0,o.up)("q-select"),h=(0,o.up)("q-toolbar"),g=(0,o.up)("q-btn"),E=(0,o.up)("q-td"),k=(0,o.up)("q-tr"),f=(0,o.up)("q-table"),H=(0,o.up)("q-page"),G=(0,o.up)("q-page-container"),_=(0,o.up)("q-layout");return(0,o.wg)(),(0,o.j4)(_,{view:"hHh LpR fFf"},{default:(0,o.w5)((()=>[(0,o.Wm)(u,{reveal:"",elevated:"",class:"bg-grey-7 text-grey-4"},{default:(0,o.w5)((()=>[(0,o.Wm)(c,{title:"TubeDB monitoring",active:"/monitoring"})])),_:1}),(0,o.Wm)(G,{class:"row"},{default:(0,o.w5)((()=>[(0,o.Wm)(H,{padding:"",class:"column"},{default:(0,o.w5)((()=>[(0,o.Wm)(h,{class:"shadow-2"},{default:(0,o.w5)((()=>[(0,o.Wm)(w,{outlined:"",label:"Select monitored set",options:["Exploratories HEG"],"stack-label":"",modelValue:m.selectedSet,"onUpdate:modelValue":s[0]||(s[0]=e=>m.selectedSet=e),"options-dense":"",dense:"",style:{width:"250px"},title:"Choose one predefined monitoring set containing plots, sensors and monitoring settings."},null,8,["modelValue"]),(0,o.Wm)(w,{outlined:"",label:"Select monitored plots",options:m.monitoring_meta.plots,"stack-label":"",modelValue:m.selectedPlots,"onUpdate:modelValue":s[1]||(s[1]=e=>m.selectedPlots=e),multiple:"",clearable:"","options-dense":"",dense:"",style:{width:"250px"},title:"Leave empty to select all plots."},null,8,["options","modelValue"]),(0,o.Wm)(w,{outlined:"",label:"Select monitored sensors",options:m.monitoring_meta.sensors,"stack-label":"","option-label":"sensor",modelValue:m.selectedSensors,"onUpdate:modelValue":s[2]||(s[2]=e=>m.selectedSensors=e),multiple:"",clearable:"","options-dense":"",dense:"",style:{width:"250px"},title:"Leave empty to select all sensors."},null,8,["options","modelValue"])])),_:1}),(0,o.Wm)(g,{onClick:p.refresh,loading:m.dataLoading,icon:"refresh"},{default:(0,o.w5)((()=>[a])),_:1},8,["onClick","loading"]),(0,o.Wm)(f,{dense:"",columns:p.columns,rows:p.rows,"row-key":"plot","rows-per-page-options":[0],pagination:m.pagination,"hide-pagination":"","sort-method":p.customSort,"binary-state-sort":""},{body:(0,o.w5)((e=>[(0,o.Wm)(k,{props:e},{default:(0,o.w5)((()=>[(0,o.Wm)(E,{key:"plot",props:e,class:(0,n.C_)(p.plotClass(e.row))},{default:(0,o.w5)((()=>[(0,o._)("b",null,(0,n.zw)(e.row.plot),1)])),_:2},1032,["props","class"]),((0,o.wg)(!0),(0,o.iD)(o.HY,null,(0,o.Ko)(p.sensorColumns,(s=>((0,o.wg)(),(0,o.j4)(E,{props:e,key:s.sensor,class:(0,n.C_)(p.cellClass(s,e.row)),title:e.row.plot+" "+s.sensor},{default:(0,o.w5)((()=>[s.number?((0,o.wg)(),(0,o.iD)("span",r,(0,n.zw)(-99999===e.row[s.sensor]?"":e.row[s.sensor].toFixed(2)),1)):((0,o.wg)(),(0,o.iD)("i",l,(0,n.zw)(0===e.row[s.sensorTimestamp]?"":e.row[s.sensor]),1))])),_:2},1032,["props","class","title"])))),128))])),_:2},1032,["props"])])),_:1},8,["columns","rows","pagination","sort-method"]),void 0===m.data?((0,o.wg)(),(0,o.iD)("div",i," Click refresh button to load data! ")):(0,o.kq)("",!0)])),_:1})])),_:1})])),_:1})}t(702),t(3269);var m=t(3100),p=t(3828);const c={components:{pagesToolbar:p.Z},data(){return{data:void 0,dataLoading:!1,pagination:{page:1,rowsPerPage:0},monitoring_meta:{plots:["HEG01","HEG02","HEG03","HEG04","HEG05","HEG06","HEG07","HEG08","HEG09","HEG10","HEG11","HEG12","HEG13","HEG14","HEG15","HEG16","HEG17","HEG18","HEG19","HEG20","HEG21","HEG22","HEG23","HEG24","HEG25","HEG26","HEG27","HEG28","HEG29","HEG30","HEG31","HEG32","HEG33","HEG34","HEG35","HEG36","HEG37","HEG38","HEG39","HEG40","HEG41","HEG42","HEG43","HEG44","HEG45","HEG46","HEG47","HEG48","HEG49","HEG50"],sensors:[{sensor:"UB",ok:[12.2,14.9],warn:[11.9,14.9]},{sensor:"Ta_200",ok:[-20,35],warn:[-40,40]},{sensor:"Ta_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_05",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_20",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_50",ok:[-20,35],warn:[-40,40]},{sensor:"SM_10",ok:[1,65],warn:[0,70]},{sensor:"SM_20",ok:[1,65],warn:[0,70]},{sensor:"rH_200",ok:[15,100],warn:[0,100]},{sensor:"LWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"LWUR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWUR_300",ok:[0,1e3],warn:[0,1500]}]},selectedSet:"Exploratories HEG",selectedSensors:void 0,selectedPlots:void 0}},computed:{...(0,m.Se)({apiGET:"apiGET"}),columns(){let e=[{name:"plot",field:"plot",label:"Plot",sortable:!0}];return void 0!==this.monitoring_meta&&void 0!==this.data&&this.data.sensors.forEach((s=>{const t=this.monitoring_meta.sensors.find((e=>e.sensor===s)),o=t.sensor+".datetime";e.push({name:o,field:o,label:"datetime",sortable:!0}),e.push({name:t.sensor,field:t.sensor,label:t.sensor,sortable:!0})})),e},columnMap(){let e={plot:{sortType:"text"}};return void 0!==this.monitoring_meta&&this.monitoring_meta.sensors.forEach((s=>{const t=s.sensor+".datetime";e[s.sensor]={sortType:"number"},e[t]={sortType:"text"}})),e},sensorColumns(){let e=[];if(void 0!==this.monitoring_meta&&void 0!==this.data){const s=this.data.sensors,t=this.monitoring_meta.sensors;s.forEach((s=>{const o=t.find((e=>e.sensor===s));if(o){const s=o.sensor+".datetime",t=o.sensor+".timestamp";e.push({sensor:s,datetime:!0,number:!1,sensorTimestamp:t}),e.push({sensor:o.sensor,datetime:!1,number:!0,ok:o.ok,warn:o.warn,sensorTimestamp:t})}else console.log("sensor not found "+s)}))}return e},rows(){if(void 0===this.data)return[];const e=this.data.sensors;let s=this.data.measurements.map((s=>{let t={plot:s.plot};const o=s.datetime,n=s.value,a=s.timestamp;for(let r=0;r<e.length;r++){const s=e[r],l=s+".datetime",i=s+".timestamp";t[l]=o[r],t[s]=n[r],t[i]=a[r]}return t}));return s},timestampNow(){return void 0===this.data?0:this.data.timestamp}},methods:{async refresh(){try{const e=new URLSearchParams;this.selectedPlots?this.selectedPlots.forEach((s=>e.append("plot",s))):this.monitoring_meta.plots.forEach((s=>e.append("plot",s))),this.selectedSensors?this.selectedSensors.forEach((s=>e.append("sensor",s.sensor))):this.monitoring_meta.sensors.forEach((s=>e.append("sensor",s.sensor))),this.dataLoading=!0;const s=await this.apiGET(["tsdb","monitoring"],{params:e});this.data=s.data}catch(e){this.data=void 0,console.log(e),this.$q.notify({message:"Error loading data.",type:"negative"})}finally{this.dataLoading=!1}},cellClass(e,s){const t=s[e.sensor],o=s[e.sensorTimestamp],n=this.timestampNow-o,a=n>1e5||n<-1440,r=n>2e5||n<-2880;return e.number?-99999===t?"":e.ok?e.ok[0]<=t&&t<=e.ok[1]?a?"sensor-ok-outdated":"sensor-ok":e.warn&&e.warn[0]<=t&&t<=e.warn[1]?a?"sensor-warn-outdated":"sensor-warn":a?"sensor-error-outdated":"sensor-error":e.warn?e.warn[0]<=t&&t<=e.warn[1]?a?"sensor-warn-outdated":"sensor-warn":a?"sensor-error-outdated":"sensor-error":a?"sensor-ok-outdated":"sensor-ok":e.datetime?a?r?"time-error":"time-warn":"time-ok":""},plotClass(e){let s="sensor-ok";return this.sensorColumns.forEach((t=>{const o=e[t.sensor],n=e[t.sensorTimestamp],a=this.timestampNow-n,r=a>1e5||a<-1440,l=a>2e5||a<-2880;t.number?-99999===o||(t.ok?t.ok[0]<=o&&o<=t.ok[1]||(t.warn&&t.warn[0]<=o&&o<=t.warn[1]?"sensor-ok"===s&&(s="sensor-warn"):s="sensor-error"):t.warn&&(t.warn[0]<=o&&o<=t.warn[1]?"sensor-ok"===s&&(s="sensor-warn"):s="sensor-error")):t.datetime&&(0===n||r&&(l?s="sensor-error":"sensor-ok"===s&&(s="sensor-warn")))})),s},customSort(e,s,t){const o=[...e];if(s){const e=this.columnMap[s];let n;switch(e.sortType){case"number":n=t?(e,t)=>parseFloat(t[s])-parseFloat(e[s]):(e,t)=>parseFloat(e[s])-parseFloat(t[s]);break;default:n=t?(e,t)=>t[s]>e[s]?1:t[s]<e[s]?-1:0:(e,t)=>e[s]>t[s]?1:e[s]<t[s]?-1:0}void 0!==n&&o.sort(n)}return o}},watch:{},async mounted(){}};var u=t(1639),w=t(249),h=t(6602),g=t(2133),E=t(9885),k=t(1663),f=t(5959),H=t(4455),G=t(7580),_=t(9546),b=t(7220),v=t(9984),y=t.n(v);const T=(0,u.Z)(c,[["render",d],["__scopeId","data-v-4fe5cc1e"]]),S=T;y()(c,"components",{QLayout:w.Z,QHeader:h.Z,QPageContainer:g.Z,QPage:E.Z,QToolbar:k.Z,QSelect:f.Z,QBtn:H.Z,QTable:G.Z,QTr:_.Z,QTd:b.Z})}}]);