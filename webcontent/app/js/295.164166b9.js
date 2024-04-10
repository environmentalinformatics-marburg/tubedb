"use strict";(globalThis["webpackChunkapp"]=globalThis["webpackChunkapp"]||[]).push([[295],{2914:(t,e,a)=>{a.d(e,{A:()=>F});var l=a(1758),o=a(8790);function s(t,e,a,s,i,n){const r=(0,l.g2)("pages-menu"),d=(0,l.g2)("q-space"),p=(0,l.g2)("q-toolbar-title"),u=(0,l.g2)("q-toolbar");return(0,l.uX)(),(0,l.Wv)(u,{class:"bg-grey-1 text-grey-8"},{default:(0,l.k6)((()=>[(0,l.bF)(r,{active:a.active},null,8,["active"]),(0,l.bF)(d),(0,l.bF)(p,{class:"title"},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(a.title),1)])),_:1}),(0,l.bF)(d)])),_:1})}function i(t,e,a,s,i,n){const r=(0,l.g2)("q-item-section"),d=(0,l.g2)("q-item"),p=(0,l.g2)("q-list"),u=(0,l.g2)("q-menu"),g=(0,l.g2)("q-btn");return(0,l.uX)(),(0,l.Wv)(g,{flat:"",round:"",dense:"",icon:"menu"},{default:(0,l.k6)((()=>[(0,l.bF)(u,{"transition-show":"scale","transition-hide":"scale",class:"bg-grey-1 text-grey-8"},{default:(0,l.k6)((()=>[(0,l.bF)(p,{style:{"min-width":"100px"}},{default:(0,l.k6)((()=>[((0,l.uX)(!0),(0,l.CE)(l.FK,null,(0,l.pI)(i.items,(t=>((0,l.uX)(),(0,l.Wv)(d,{key:t.title,to:t.link,clickable:"",class:(0,o.C4)({activeitem:a.active===t.link})},{default:(0,l.k6)((()=>[(0,l.bF)(r,null,{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(t.title),1)])),_:2},1024)])),_:2},1032,["to","class"])))),128))])),_:1})])),_:1})])),_:1})}const n={name:"pages-menu",props:["active"],data(){return{items:[{title:"Overview",link:"/overview"},{title:"Metadata",link:"/model"},{title:"Diagrams",link:"/diagram"},{title:"Monitoring",link:"/monitoring"},{title:"Plot status",link:"/plot_status"}]}},computed:{},methods:{},watch:{},async mounted(){}};var r=a(2807),d=a(7954),p=a(6929),u=a(4514),g=a(5329),c=a(3418),h=a(8582),m=a.n(h);const b=(0,r.A)(n,[["render",i],["__scopeId","data-v-003556f6"]]),f=b;m()(n,"components",{QBtn:d.A,QMenu:p.A,QList:u.A,QItem:g.A,QItemSection:c.A});const k={name:"pages-toolbar",props:["title","active"],components:{pagesMenu:f},data(){return{}},computed:{},methods:{},watch:{},async mounted(){}};var v=a(9351),_=a(1173),w=a(6769);const y=(0,r.A)(k,[["render",s],["__scopeId","data-v-1fb16e4f"]]),F=y;m()(k,"components",{QToolbar:v.A,QSpace:_.A,QToolbarTitle:w.A})},5295:(t,e,a)=>{a.r(e),a.d(e,{default:()=>et});var l=a(1758),o=a(9104),s=a(8790);const i=["href"],n={key:1},r={key:0},d={key:1},p={key:2};function u(t,e,a,u,g,c){const h=(0,l.g2)("pages-toolbar"),m=(0,l.g2)("q-header"),b=(0,l.g2)("q-select"),f=(0,l.g2)("q-icon"),k=(0,l.g2)("q-input"),v=(0,l.g2)("q-td"),_=(0,l.g2)("q-table"),w=(0,l.g2)("q-card"),y=(0,l.g2)("q-page"),F=(0,l.g2)("q-spinner-ios"),A=(0,l.g2)("q-btn"),Q=(0,l.g2)("q-page-container"),q=(0,l.g2)("plot-status-dialog"),C=(0,l.g2)("q-layout");return(0,l.uX)(),(0,l.Wv)(C,{view:"hHh LpR fFf"},{default:(0,l.k6)((()=>[(0,l.bF)(m,{reveal:"",elevated:"",class:"bg-grey-7 text-grey-4"},{default:(0,l.k6)((()=>[(0,l.bF)(h,{title:"Plot status",active:"/plot_status"})])),_:1}),(0,l.bF)(Q,{class:"row justify-center"},{default:(0,l.k6)((()=>[void 0!==t.model?((0,l.uX)(),(0,l.Wv)(y,{key:0,padding:"",class:"column"},{default:(0,l.k6)((()=>[(0,l.bF)(w,{bordered:"",class:"overview-item"},{default:(0,l.k6)((()=>[(0,l.bF)(_,{rows:c.rows,columns:g.columns,pagination:g.pagination,"hide-bottom":"",dense:"",loading:g.rowsLoading,"row-key":"plot","binary-state-sort":"",onRowClick:c.onRowClick,"table-header-class":"table-header",filter:g.filter},{"top-left":(0,l.k6)((()=>[(0,l.bF)(b,{modelValue:g.project,"onUpdate:modelValue":e[0]||(e[0]=t=>g.project=t),options:c.projects,label:"Project","option-label":"title",filled:"",readonly:c.projects.length<=1},null,8,["modelValue","options","readonly"]),(0,l.eW)(" / "),(0,l.bF)(b,{modelValue:g.group,"onUpdate:modelValue":e[1]||(e[1]=t=>g.group=t),options:c.groups,label:"Group","option-label":"title",filled:"",readonly:c.groups.length<=1},null,8,["modelValue","options","readonly"]),(0,l.eW)(" / "),(0,l.bF)(b,{modelValue:g.plot,"onUpdate:modelValue":e[2]||(e[2]=t=>g.plot=t),options:c.plots,label:"Plot","option-label":"title",filled:"",readonly:c.plots.length<=1,"display-value":void 0===g.plot||null===g.plot||0===g.plot.length?"(all)":1===g.plot.length?g.plot[0]:"(multiple)",multiple:"",clearable:""},null,8,["modelValue","options","readonly","display-value"])])),"top-right":(0,l.k6)((()=>[(0,l.bF)(k,{debounce:"300",modelValue:g.filter,"onUpdate:modelValue":e[3]||(e[3]=t=>g.filter=t),"stack-label":"",label:"Search",filled:"",clearable:""},{append:(0,l.k6)((()=>[(0,l.bF)(f,{name:"search"})])),_:1},8,["modelValue"])])),"body-cell-plot":(0,l.k6)((a=>[(0,l.bF)(v,{props:a},{default:(0,l.k6)((()=>[(0,l.Lk)("a",{href:t.api("content/visualisation_meta/visualisation_meta.html?pinned_project="+g.project.id+"&pinned_plot="+a.row.plot),target:"_blank",title:"Open timeseries diagram in a new tab."},[(0,l.bF)(f,{name:"timeline",onClick:e[4]||(e[4]=(0,o.D$)((()=>{}),["stop"]))})],8,i),(0,l.eW)(" "+(0,s.v_)(a.row.plot),1)])),_:2},1032,["props"])])),"body-cell-first_datetime":(0,l.k6)((t=>[(0,l.bF)(v,{props:t},{default:(0,l.k6)((()=>[(0,l.eW)((0,s.v_)(t.row.first_date)+" ",1),(0,l.Lk)("span",null,(0,s.v_)(t.row.first_time),1)])),_:2},1032,["props"])])),"body-cell-last_datetime":(0,l.k6)((t=>[(0,l.bF)(v,{props:t},{default:(0,l.k6)((()=>[(0,l.eW)((0,s.v_)(t.row.last_date)+" ",1),(0,l.Lk)("span",null,(0,s.v_)(t.row.last_time),1)])),_:2},1032,["props"])])),"body-cell-voltage":(0,l.k6)((t=>[(0,l.bF)(v,{props:t},{default:(0,l.k6)((()=>[(0,l.eW)((0,s.v_)(void 0===t.row.voltage?"-":t.row.voltage.toFixed(2)),1)])),_:2},1032,["props"])])),_:1},8,["rows","columns","pagination","loading","onRowClick","filter"])])),_:1})])),_:1})):((0,l.uX)(),(0,l.CE)("div",n,[t.modelLoading?((0,l.uX)(),(0,l.CE)("div",r,[(0,l.bF)(F,{color:"primary",size:"2em"}),(0,l.eW)(" Loading metadata... ")])):t.modelError?((0,l.uX)(),(0,l.CE)("div",d,[(0,l.eW)("Error loading metadata. "),(0,l.bF)(A,{onClick:e[5]||(e[5]=e=>t.$store.dispatch("model/refresh"))},{default:(0,l.k6)((()=>[(0,l.eW)("try again")])),_:1})])):((0,l.uX)(),(0,l.CE)("div",p,"Metadata not loaded."))]))])),_:1}),(0,l.bF)(q,{ref:"plotStatusDialog",onChanged:c.refresh},null,8,["onChanged"])])),_:1})}a(7396),a(923),a(9502);var g=a(6980),c=a(2914);const h={class:"info"},m={key:0},b={key:1},f={key:2},k={key:3},v={key:4};function _(t,e,a,o,i,n){const r=(0,l.g2)("q-space"),d=(0,l.g2)("q-tooltip"),p=(0,l.g2)("q-btn"),u=(0,l.g2)("q-bar"),g=(0,l.g2)("q-linear-progress"),c=(0,l.g2)("q-header"),_=(0,l.g2)("q-item-section"),w=(0,l.g2)("q-item"),y=(0,l.g2)("q-icon"),F=(0,l.g2)("q-input"),A=(0,l.g2)("q-list"),Q=(0,l.g2)("q-item-label"),q=(0,l.g2)("q-separator"),C=(0,l.g2)("q-expansion-item"),W=(0,l.g2)("q-page-container"),L=(0,l.g2)("q-card-actions"),x=(0,l.g2)("q-card"),E=(0,l.g2)("q-footer"),S=(0,l.g2)("q-layout"),T=(0,l.g2)("q-dialog"),j=(0,l.gN)("close-popup");return(0,l.uX)(),(0,l.Wv)(T,{modelValue:i.shown,"onUpdate:modelValue":e[3]||(e[3]=t=>i.shown=t),maximized:!0},{default:(0,l.k6)((()=>[(0,l.bF)(S,{view:"hHh lpR fFf",class:"bg-grey-1"},{default:(0,l.k6)((()=>[(0,l.bF)(c,{elevated:"",class:"bg-grey-1 text-black"},{default:(0,l.k6)((()=>[(0,l.bF)(u,null,{default:(0,l.k6)((()=>[(0,l.eW)(" Status "),(0,l.bF)(r),(0,l.eW)(" "+(0,s.v_)(i.plot)+" ",1),(0,l.bF)(r),(0,l.bo)(((0,l.uX)(),(0,l.Wv)(p,{dense:"",flat:"",icon:"close"},{default:(0,l.k6)((()=>[(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.eW)("Close")])),_:1})])),_:1})),[[j]])])),_:1}),i.loading?((0,l.uX)(),(0,l.Wv)(g,{key:0,indeterminate:""})):(0,l.Q3)("",!0)])),_:1}),i.loading||i.error?(0,l.Q3)("",!0):((0,l.uX)(),(0,l.Wv)(W,{key:0,class:"text-black"},{default:(0,l.k6)((()=>[(0,l.bF)(A,null,{default:(0,l.k6)((()=>[(0,l.bF)(w,null,{default:(0,l.k6)((()=>[(0,l.bF)(_,{side:""},{default:(0,l.k6)((()=>[(0,l.bF)(p,{flat:"",round:"",color:"primary",icon:"timeline",href:t.api("content/visualisation_meta/visualisation_meta.html?pinned_project="+t.project.id+"&pinned_plot="+i.plot),target:"_blank",title:"Open timeseries diagram in a new tab."},null,8,["href"])])),_:1}),(0,l.bF)(_,null,{default:(0,l.k6)((()=>[(0,l.Lk)("span",h,[void 0!==i.row.first_date?((0,l.uX)(),(0,l.CE)("span",m,[(0,l.eW)("Measurements from "),(0,l.Lk)("span",null,(0,s.v_)(i.row.first_date),1),(0,l.eW)(),(0,l.Lk)("span",null,(0,s.v_)(i.row.first_time),1)])):(0,l.Q3)("",!0),void 0!==i.row.first_date?((0,l.uX)(),(0,l.CE)("span",b,[(0,l.eW)(" to "),(0,l.Lk)("span",null,(0,s.v_)(i.row.last_date),1),(0,l.eW)(),(0,l.Lk)("span",null,(0,s.v_)(i.row.last_time),1)])):(0,l.Q3)("",!0),void 0!==i.row.elapsed_days?((0,l.uX)(),(0,l.CE)("span",f,[(0,l.eW)(" Elapsed days "),(0,l.Lk)("span",null,(0,s.v_)(i.row.elapsed_days),1)])):(0,l.Q3)("",!0),void 0!==i.row.voltage?((0,l.uX)(),(0,l.CE)("span",k,[(0,l.eW)(" Latest voltage "),(0,l.Lk)("span",null,(0,s.v_)(i.row.voltage),1)])):(0,l.Q3)("",!0),void 0!==i.row.author?((0,l.uX)(),(0,l.CE)("span",v,[(0,l.eW)(" Status author "),(0,l.Lk)("span",null,(0,s.v_)(i.row.author),1),(0,l.eW)(" date "),(0,l.Lk)("span",null,(0,s.v_)(i.row.datetime),1)])):(0,l.Q3)("",!0)])])),_:1})])),_:1}),(0,l.bF)(w,null,{default:(0,l.k6)((()=>[(0,l.bF)(_,{side:""},{default:(0,l.k6)((()=>[(0,l.bF)(y,{name:"cell_tower"})])),_:1}),(0,l.bF)(_,null,{default:(0,l.k6)((()=>[(0,l.bF)(F,{outlined:"",modelValue:i.row.status,"onUpdate:modelValue":e[0]||(e[0]=t=>i.row.status=t),label:"Status","stack-label":"",dense:""},null,8,["modelValue"])])),_:1})])),_:1}),(0,l.bF)(w,null,{default:(0,l.k6)((()=>[(0,l.bF)(_,{side:""},{default:(0,l.k6)((()=>[(0,l.bF)(y,{name:"task"})])),_:1}),(0,l.bF)(_,null,{default:(0,l.k6)((()=>[(0,l.bF)(F,{outlined:"",modelValue:i.row.tasks,"onUpdate:modelValue":e[1]||(e[1]=t=>i.row.tasks=t),label:"Tasks","stack-label":"",dense:""},null,8,["modelValue"])])),_:1})])),_:1}),(0,l.bF)(w,null,{default:(0,l.k6)((()=>[(0,l.bF)(_,{side:""},{default:(0,l.k6)((()=>[(0,l.bF)(y,{name:"text_snippet"})])),_:1}),(0,l.bF)(_,null,{default:(0,l.k6)((()=>[(0,l.bF)(F,{outlined:"",modelValue:i.row.notes,"onUpdate:modelValue":e[2]||(e[2]=t=>i.row.notes=t),label:"Notes","stack-label":"",dense:"",type:"textarea"},null,8,["modelValue"])])),_:1})])),_:1})])),_:1}),(0,l.bF)(C,{dense:"","expand-separator":"",icon:"history",label:"History",disable:void 0===i.row.history||0===i.row.history.length},{default:(0,l.k6)((()=>[void 0!==i.row.history&&i.row.history.length>0?((0,l.uX)(),(0,l.Wv)(A,{key:0},{default:(0,l.k6)((()=>[((0,l.uX)(!0),(0,l.CE)(l.FK,null,(0,l.pI)(i.row.history.slice().reverse(),(t=>((0,l.uX)(),(0,l.CE)(l.FK,{key:t.datetime},[(0,l.bF)(w,null,{default:(0,l.k6)((()=>[(0,l.bF)(_,null,{default:(0,l.k6)((()=>[(0,l.bF)(Q,null,{default:(0,l.k6)((()=>[(0,l.Lk)("b",null,(0,s.v_)(t.status),1),(0,l.eW)(" "+(0,s.v_)(t.tasks),1)])),_:2},1024),(0,l.bF)(Q,{caption:""},{default:(0,l.k6)((()=>[(0,l.eW)((0,s.v_)(t.notes),1)])),_:2},1024)])),_:2},1024),(0,l.bF)(_,{top:"",side:""},{default:(0,l.k6)((()=>[(0,l.bF)(Q,{caption:""},{default:(0,l.k6)((()=>[(0,l.Lk)("i",null,(0,s.v_)(t.author),1),(0,l.eW)(" "+(0,s.v_)(t.datetime),1)])),_:2},1024)])),_:2},1024)])),_:2},1024),(0,l.bF)(q,{spaced:"",inset:""})],64)))),128))])),_:1})):(0,l.Q3)("",!0)])),_:1},8,["disable"])])),_:1})),i.loading||i.error?(0,l.Q3)("",!0):((0,l.uX)(),(0,l.Wv)(E,{key:1,elevated:"",class:"bg-grey-1"},{default:(0,l.k6)((()=>[(0,l.bF)(x,null,{default:(0,l.k6)((()=>[(0,l.bF)(L,{align:"right"},{default:(0,l.k6)((()=>[(0,l.bo)((0,l.bF)(p,{flat:"",label:"Cancel",color:"primary"},null,512),[[j]]),(0,l.bF)(p,{flat:"",label:"Save",color:"primary",onClick:n.onSubmit,loading:i.submitting},null,8,["onClick","loading"])])),_:1})])),_:1})])),_:1}))])),_:1})])),_:1},8,["modelValue"])}const w={props:[],components:{},data(){return{shown:!1,plot:void 0,loading:!1,error:!1,submitting:!1,row:{}}},computed:{...(0,g.L8)({api:"api",apiGET:"apiGET",apiPOST:"apiPOST"})},methods:{show(t,e){this.shown=!0,this.project=t,this.plot=e,this.refresh()},async refresh(){try{this.loading=!0;const t=new URLSearchParams;t.append("plot",this.plot),t.append("plot_status",""),t.append("history","");const e=await this.apiGET(["tsdb","status"],{params:t});let a=e.data;a.forEach((t=>{{const e=t.first_datetime.split("T");t.first_date=e[0],t.first_time=e[1]}{const e=t.last_datetime.split("T");t.last_date=e[0],t.last_time=e[1]}})),this.row=0===a.length?{}:a[0],this.row.plot!==this.plot&&(this.error=!0,this.plot=void 0,this.row={},this.$q.notify({message:"Error loading data.",type:"negative"}))}catch(t){this.error=!0,this.plot=void 0,this.rows=[],console.log(t),this.$q.notify({message:"Error loading data.",type:"negative"})}finally{this.loading=!1}},async onSubmit(){try{this.submitting=!0,await this.apiPOST(["tsdb","status"],this.row),this.$emit("changed"),this.shown=!1}catch(t){console.log(t),this.$q.notify({message:"Error submitting data.",type:"negative"})}finally{this.submitting=!1}}},watch:{},async mounted(){}};var y=a(2807),F=a(7201),A=a(3333),Q=a(133),q=a(3643),C=a(1173),W=a(7954),L=a(8151),x=a(2876),E=a(1102),S=a(4514),T=a(5329),j=a(3418),V=a(4609),X=a(8020),I=a(6586),R=a(5779),M=a(7015),P=a(8889),O=a(7569),U=a(6334),$=a(9197),G=a(8582),D=a.n(G);const H=(0,y.A)(w,[["render",_],["__scopeId","data-v-3dbe09ee"]]),N=H;D()(w,"components",{QDialog:F.A,QLayout:A.A,QHeader:Q.A,QBar:q.A,QSpace:C.A,QBtn:W.A,QTooltip:L.A,QLinearProgress:x.A,QPageContainer:E.A,QList:S.A,QItem:T.A,QItemSection:j.A,QIcon:V.A,QInput:X.A,QExpansionItem:I.A,QItemLabel:R.A,QSeparator:M.A,QFooter:P.A,QCard:O.A,QCardActions:U.A}),D()(w,"directives",{ClosePopup:$.A});const B={components:{pagesToolbar:c.A,plotStatusDialog:N},data(){return{columns:[{name:"plot",field:"plot",label:"Plot",headerStyle:"text-align: center; border-right: 1px solid #c6c6c6;",align:"left",sortable:!0,classes:"plot"},{name:"first_datetime",field:"first_datetime",label:"Earliest data",headerStyle:"text-align: center;",align:"left",sortable:!0},{name:"last_datetime",field:"last_datetime",label:"Latest data",headerStyle:"text-align: center;",align:"left",sortable:!0},{name:"elapsed_days",field:"elapsed_days",label:"Elapsed days",headerStyle:"text-align: center;",align:"right",sortable:!0,classes:t=>this.elapsedClass(t.elapsed_days)},{name:"voltage",field:"voltage",label:"Latest voltage",headerStyle:"text-align: center;",align:"right",sortable:!0,classes:this.voltageClass},{name:"status",field:"status",label:"Status",headerStyle:"text-align: center; min-width: 150px; max-width: 150px;",align:"left",sortable:!0,style:"min-width: 150px; max-width: 150px; overflow: hidden; text-overflow: ellipsis; background-color: rgba(0, 0, 255, 0.02);"},{name:"tasks",field:"tasks",label:"Tasks",headerStyle:"text-align: center; min-width: 150px; max-width: 150px;",align:"left",sortable:!0,style:"min-width: 300px; max-width: 300px; overflow: hidden; text-overflow: ellipsis; background-color: #fff;"}],pagination:{rowsPerPage:0,sortBy:"last_datetime"},rawRows:[],rowsLoading:!1,project:void 0,group:void 0,plot:void 0,filter:void 0}},computed:{...(0,g.aH)({model:t=>t.model.data,modelLoading:t=>t.model.loading,modelError:t=>t.model.error}),...(0,g.L8)({api:"api",apiGET:"apiGET"}),projects(){return void 0===this.model?[]:Object.values(this.model.projects)},groups(){return void 0===this.project?[]:this.project.groups.map((t=>this.model.groups[t]))},plots(){return void 0===this.group?[]:this.group.plots},rows(){if(void 0===this.rawRows||0===this.rawRows.length)return[];if(void 0===this.plot||null===this.plot||0===this.plot.length)return this.rawRows;if(1===this.plot.length){const t=this.plot[0];return this.rawRows.filter((e=>e.plot===t))}const t=this.plot;return this.rawRows.filter((e=>t.includes(e.plot)))}},methods:{async refresh(){if(void 0!==this.group)try{this.rowsLoading=!0;const t=new URLSearchParams;t.append("generalstation",this.group.id),t.append("plot_status","");const e=await this.apiGET(["tsdb","status"],{params:t});let a=e.data;a.forEach((t=>{{const e=t.first_datetime.split("T");t.first_date=e[0],t.first_time=e[1]}{const e=t.last_datetime.split("T");t.last_date=e[0],t.last_time=e[1]}})),this.rawRows=a}catch(t){this.rawRows=[],console.log(t),this.$q.notify({message:"Error loading data.",type:"negative"})}finally{this.rowsLoading=!1}else this.rawRows=[]},elapsedClass(t){let e="timeMarkOneMonth";return e=t>365?"timeMarkLost":t>28?"timeMarkOneMonth":t>14?"timeMarkTwoWeeks":t>7?"timeMarkOneWeek":"timeMarkNow",e},voltageClass:function(t){const e=t.voltage;let a="voltageMarkNaN";return void 0!==e&&e>=0&&e<t.voltage_min_error&&(a=t.voltage_min_good<=e?"voltageMarkOK":t.voltage_min_watch<=e?"voltageMarkWARN":"voltageMarkCRITICAL"),a},onRowClick(t,e){this.$refs.plotStatusDialog.show(this.project,e.plot)}},watch:{projects:{handler(){void 0===this.projects||0===this.projects.length?this.project=void 0:(this.projects.length,this.project=this.projects[0])},immediate:!0},groups:{handler(){void 0===this.groups||0===this.groups.length?this.group=void 0:(this.groups.length,this.group=this.groups[0])},immediate:!0},group(){this.refresh()},plots(){void 0===this.plots||0===this.plots.length?this.plot=void 0:1===this.plots.length?this.plot=this.plots[0]:this.plot=void 0}},async mounted(){this.$store.dispatch("model/init"),this.refresh()}};var K=a(3177),z=a(9711),J=a(5685),Y=a(7109),Z=a(3222);const tt=(0,y.A)(B,[["render",u],["__scopeId","data-v-4ee180b8"]]),et=tt;D()(B,"components",{QLayout:A.A,QHeader:Q.A,QPageContainer:E.A,QPage:K.A,QCard:O.A,QTable:z.A,QSelect:J.A,QInput:X.A,QIcon:V.A,QTd:Y.A,QSpinnerIos:Z.A,QBtn:W.A})}}]);