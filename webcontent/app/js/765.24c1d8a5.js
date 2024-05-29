"use strict";(globalThis["webpackChunkapp"]=globalThis["webpackChunkapp"]||[]).push([[765],{2914:(e,t,s)=>{s.d(t,{A:()=>x});var l=s(1758),o=s(8790);function i(e,t,s,i,a,n){const r=(0,l.g2)("pages-menu"),d=(0,l.g2)("q-space"),u=(0,l.g2)("q-toolbar-title"),c=(0,l.g2)("q-toolbar");return(0,l.uX)(),(0,l.Wv)(c,{class:"bg-grey-1 text-grey-8"},{default:(0,l.k6)((()=>[(0,l.bF)(r,{active:s.active},null,8,["active"]),(0,l.bF)(d),(0,l.bF)(u,{class:"title"},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(s.title),1)])),_:1}),(0,l.bF)(d)])),_:1})}function a(e,t,s,i,a,n){const r=(0,l.g2)("q-item-section"),d=(0,l.g2)("q-item"),u=(0,l.g2)("q-list"),c=(0,l.g2)("q-menu"),m=(0,l.g2)("q-btn");return(0,l.uX)(),(0,l.Wv)(m,{flat:"",round:"",dense:"",icon:"menu"},{default:(0,l.k6)((()=>[(0,l.bF)(c,{"transition-show":"scale","transition-hide":"scale",class:"bg-grey-1 text-grey-8"},{default:(0,l.k6)((()=>[(0,l.bF)(u,{style:{"min-width":"100px"}},{default:(0,l.k6)((()=>[((0,l.uX)(!0),(0,l.CE)(l.FK,null,(0,l.pI)(a.items,(e=>((0,l.uX)(),(0,l.Wv)(d,{key:e.title,to:e.link,clickable:"",class:(0,o.C4)({activeitem:s.active===e.link})},{default:(0,l.k6)((()=>[(0,l.bF)(r,null,{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(e.title),1)])),_:2},1024)])),_:2},1032,["to","class"])))),128))])),_:1})])),_:1})])),_:1})}const n={name:"pages-menu",props:["active"],data(){return{items:[{title:"Overview",link:"/overview"},{title:"Metadata",link:"/model"},{title:"Diagrams",link:"/diagram"},{title:"Monitoring",link:"/monitoring"},{title:"Plot status",link:"/plot_status"}]}},computed:{},methods:{},watch:{},async mounted(){}};var r=s(2807),d=s(1693),u=s(4436),c=s(3999),m=s(124),h=s(5173),g=s(8582),p=s.n(g);const v=(0,r.A)(n,[["render",a],["__scopeId","data-v-003556f6"]]),f=v;p()(n,"components",{QBtn:d.A,QMenu:u.A,QList:c.A,QItem:m.A,QItemSection:h.A});const b={name:"pages-toolbar",props:["title","active"],components:{pagesMenu:f},data(){return{}},computed:{},methods:{},watch:{},async mounted(){}};var k=s(6914),_=s(3676),y=s(9150);const M=(0,r.A)(b,[["render",i],["__scopeId","data-v-1fb16e4f"]]),x=M;p()(b,"components",{QToolbar:k.A,QSpace:_.A,QToolbarTitle:y.A})},8765:(e,t,s)=>{s.r(t),s.d(t,{default:()=>ue});var l=s(1758),o=s(8790),i=s(9104);const a=e=>((0,l.Qi)("data-v-72f5beff"),e=e(),(0,l.jt)(),e),n={class:"fit row"},r={style:{color:"grey"}},d={key:1,class:"fit"},u={key:0},c={key:1},m={key:2},h={class:"drawerChanger"},g={style:{position:"relative"}},p={key:0,style:{position:"absolute",top:"70px",left:"50px"}},v={key:1,style:{position:"absolute",top:"100px",left:"100px"}},f={style:{"text-align":"right","margin-right":"10px"}},b=a((()=>(0,l.Lk)("b",null,"(de-)activate",-1))),k={style:{"margin-top":"10px","margin-left":"10px"}},_=a((()=>(0,l.Lk)("table",null,[(0,l.Lk)("tr",null,[(0,l.Lk)("td",{style:{"padding-right":"10px","text-align":"center"}},[(0,l.Lk)("b",null,"Zoom in/out")]),(0,l.Lk)("td",null,"Place mouse on diagram and rotate the mouse wheel.")]),(0,l.Lk)("tr",null,[(0,l.Lk)("td",{style:{"padding-right":"10px","text-align":"center"}},[(0,l.Lk)("b",null,"Move in time")]),(0,l.Lk)("td",null,"Place mouse on diagram, press and hold left mouse button and move mouse left / right on the diagram.")]),(0,l.Lk)("tr",null,[(0,l.Lk)("td",{style:{"padding-right":"10px","text-align":"center"}},[(0,l.Lk)("b",null,"Inspect timeseries values")]),(0,l.Lk)("td",null,"Move mouse over diagram without mouse buttons pressed to show time / measurement values.")])],-1))),y=[_],M={style:{"text-align":"right","margin-right":"10px"}};function x(e,t,s,a,_,x){const S=(0,l.g2)("pages-toolbar"),F=(0,l.g2)("q-header"),T=(0,l.g2)("q-input"),w=(0,l.g2)("q-select"),P=(0,l.g2)("q-item-section"),A=(0,l.g2)("q-item"),q=(0,l.g2)("q-checkbox"),W=(0,l.g2)("q-item-label"),L=(0,l.g2)("q-separator"),C=(0,l.g2)("timeseries-selector"),Q=(0,l.g2)("q-list"),E=(0,l.g2)("q-scroll-area"),X=(0,l.g2)("q-spinner-ios"),V=(0,l.g2)("q-btn"),D=(0,l.g2)("q-drawer"),G=(0,l.g2)("q-icon"),R=(0,l.g2)("timeseries-diagram"),j=(0,l.g2)("q-page-container"),I=(0,l.g2)("q-layout"),$=(0,l.gN)("touch-pan");return(0,l.uX)(),(0,l.Wv)(I,{view:"hHh LpR fFf"},{default:(0,l.k6)((()=>[(0,l.bF)(F,{reveal:"",elevated:"",class:"bg-grey-7 text-grey-4"},{default:(0,l.k6)((()=>[(0,l.bF)(S,{title:"TubeDB Diagram",active:"/diagram"})])),_:1}),(0,l.bF)(D,{"show-if-above":"",side:"left",behavior:"desktop","content-class":"bg-grey-4",width:_.drawerWidth},{default:(0,l.k6)((()=>[(0,l.Lk)("div",n,[void 0!==e.model?((0,l.uX)(),(0,l.Wv)(E,{key:0,class:"col-grow"},{default:(0,l.k6)((()=>[(0,l.bF)(Q,null,{default:(0,l.k6)((()=>[(0,l.bF)(A,{tag:"label",title:"Time format: e.g. 2024 or 2024-02 or 2024-02-28"},{default:(0,l.k6)((()=>[(0,l.bF)(P,{class:"justify-evenly",style:{display:"flex","flex-direction":"row"}},{default:(0,l.k6)((()=>[(0,l.bF)(T,{modelValue:_.startTime,"onUpdate:modelValue":t[0]||(t[0]=e=>_.startTime=e),label:"Start time","stack-label":"",borderless:"",dense:"",placeholder:void 0===_.endTime||null===_.endTime||0===_.endTime.trim().length?"(no limit)":"(start "+_.endTime.trim()+")",maxlength:"10",style:{width:"120px"},title:"Start. e.g. 2024 or 2024-02 or 2024-02-28"},null,8,["modelValue","placeholder"]),(0,l.bF)(T,{modelValue:_.endTime,"onUpdate:modelValue":t[1]||(t[1]=e=>_.endTime=e),label:"End time","stack-label":"",borderless:"",dense:"",placeholder:void 0===_.startTime||null===_.startTime||0===_.startTime.trim().length?"(no limit)":"(end "+_.startTime.trim()+")",maxlength:"10",style:{width:"120px"},title:"End. e.g. 2024 or 2024-02 or 2024-02-28"},null,8,["modelValue","placeholder"]),(0,l.bF)(w,{class:"col-grow",modelValue:_.timeAggregation,"onUpdate:modelValue":t[2]||(t[2]=e=>_.timeAggregation=e),options:["none","hour","day","week","month","year"],label:"Time aggregation","stack-label":"",borderless:"",dense:!0,"options-dense":!0,"transition-show":"scale","transition-hide":"scale",title:"Time resolution."},null,8,["modelValue"])])),_:1})])),_:1}),(0,l.bF)(A,{tag:"label"},{default:(0,l.k6)((()=>[(0,l.bF)(P,null,{default:(0,l.k6)((()=>[(0,l.bF)(w,{modelValue:_.quality,"onUpdate:modelValue":t[3]||(t[3]=e=>_.quality=e),options:x.qualities,label:"Quality checks","stack-label":"",borderless:"",dense:!0,"options-dense":!0,"option-disable":e=>"none"===_.timeAggregation&&"empirical"===e,"transition-show":"scale","transition-hide":"scale"},null,8,["modelValue","options","option-disable"])])),_:1})])),_:1}),(0,l.bF)(A,{tag:"label",style:{"user-select":"none"},disable:"none"===_.timeAggregation},{default:(0,l.k6)((()=>[(0,l.bF)(P,{avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(q,{modelValue:_.interpolation,"onUpdate:modelValue":t[4]||(t[4]=e=>_.interpolation=e),color:"teal",size:"xs",disable:"none"===_.timeAggregation},null,8,["modelValue","disable"])])),_:1}),"none"!==_.timeAggregation?((0,l.uX)(),(0,l.Wv)(P,{key:0},{default:(0,l.k6)((()=>[(0,l.bF)(W,null,{default:(0,l.k6)((()=>[(0,l.eW)("Interpolation")])),_:1})])),_:1})):((0,l.uX)(),(0,l.Wv)(P,{key:1},{default:(0,l.k6)((()=>[(0,l.bF)(W,null,{default:(0,l.k6)((()=>[(0,l.eW)("(Interpolation not available for raw data.)")])),_:1})])),_:1}))])),_:1},8,["disable"]),(0,l.bF)(L),(0,l.bF)(A,null,{default:(0,l.k6)((()=>[(0,l.bF)(C,{multiTimeseries:_.multiTimeseries,timeAggregation:_.timeAggregation,onPlotSensorChanged:t[5]||(t[5]=e=>{_.selectedPlots=e.plots,_.selectedSensors=e.sensors}),ref:"timeseriesSelector"},null,8,["multiTimeseries","timeAggregation"])])),_:1}),(0,l.bF)(L),x.plotSensorList.length>0?((0,l.uX)(),(0,l.CE)(l.FK,{key:0},[(0,l.eW)(" Selected timeseries "),(0,l.Lk)("span",r,"(max "+(0,o.v_)(_.max_timeseries)+")",1),(0,l.bF)(Q,{dense:"",separator:""},{default:(0,l.k6)((()=>[((0,l.uX)(!0),(0,l.CE)(l.FK,null,(0,l.pI)(x.plotSensorList,((t,s)=>((0,l.uX)(),(0,l.Wv)(A,{key:t.plot+"/"+t.sensor,clickable:""},{default:(0,l.k6)((()=>[(0,l.bF)(P,null,{default:(0,l.k6)((()=>[(0,l.bF)(W,null,{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(t.plot),1)])),_:2},1024),(0,l.bF)(W,{caption:"",lines:"2"},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(t.sensor),1)])),_:2},1024)])),_:2},1024),(0,l.bF)(P,{side:"",top:"",color:"yellow"},{default:(0,l.k6)((()=>[(0,l.bF)(W,{caption:"",style:(0,o.Tr)([{"background-color":"rgb(240,240,240)"},{color:e.$refs.timeseriesDiagram.timeseriesStrokes[s]}])},{default:(0,l.k6)((()=>[(0,l.eW)("["+(0,o.v_)(s+1)+"]",1)])),_:2},1032,["style"])])),_:2},1024)])),_:2},1024)))),128))])),_:1})],64)):(0,l.Q3)("",!0)])),_:1})])),_:1})):((0,l.uX)(),(0,l.CE)("div",d,[e.modelLoading?((0,l.uX)(),(0,l.CE)("div",u,[(0,l.bF)(X,{color:"primary",size:"2em"}),(0,l.eW)(" Loading metadata... ")])):e.modelError?((0,l.uX)(),(0,l.CE)("div",c,[(0,l.eW)("Error loading metadata. "),(0,l.bF)(V,{onClick:t[6]||(t[6]=t=>e.$store.dispatch("model/refresh"))},{default:(0,l.k6)((()=>[(0,l.eW)("try again")])),_:1})])):((0,l.uX)(),(0,l.CE)("div",m,"Metadata not loaded."))])),(0,l.bo)((0,l.Lk)("div",h,null,512),[[$,x.onChangeDrawerWidth,void 0,{prevent:!0,mouse:!0}]])])])),_:1},8,["width"]),(0,l.bF)(j,null,{default:(0,l.k6)((()=>[(0,l.Lk)("div",g,[_.dataRequestSentCounter>_.dataRequestReceivedCounter?((0,l.uX)(),(0,l.CE)("div",p,[(0,l.bF)(A,null,{default:(0,l.k6)((()=>[(0,l.bF)(P,{avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(G,{name:"error_outline",color:"blue-14"})])),_:1}),(0,l.bF)(P,null,{default:(0,l.k6)((()=>[(0,l.bF)(X,{color:"primary",size:"2em"}),(0,l.eW)(" Requesting data from server ... ")])),_:1})])),_:1})])):(0,l.Q3)("",!0),void 0!==_.dataRequestError?((0,l.uX)(),(0,l.CE)("div",v,[(0,l.bF)(A,null,{default:(0,l.k6)((()=>[(0,l.bF)(P,{avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(G,{name:"error_outline",color:"red-14"})])),_:1}),(0,l.bF)(P,null,{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(_.dataRequestError),1)])),_:1})])),_:1})])):(0,l.Q3)("",!0),(0,l.bF)(R,{data:_.data,timeAggregation:_.timeAggregation,highQualityDiagram:_.highQualityDiagram,ref:"timeseriesDiagram"},null,8,["data","timeAggregation","highQualityDiagram"]),(0,l.bo)((0,l.Lk)("div",f,[(0,l.eW)(" Click on the colored squares to "),b,(0,l.eW)(" that time series shown in the diagram. ")],512),[[i.aG,void 0!==_.data]]),(0,l.bo)((0,l.Lk)("div",k,y,512),[[i.aG,void 0!==_.data]]),(0,l.bo)((0,l.Lk)("div",M,[(0,l.bF)(q,{modelValue:_.highQualityDiagram,"onUpdate:modelValue":t[7]||(t[7]=e=>_.highQualityDiagram=e),color:"teal",size:"xs",title:"High quality diagram"},null,8,["modelValue"]),(0,l.eW)(" HQ ")],512),[[i.aG,void 0!==_.data]])])])),_:1})])),_:1})}s(239),s(3186),s(5531),s(5683),s(9048);var S=s(6980),F=s(2914);function T(e,t,s,i,a,n){const r=(0,l.g2)("q-select"),d=(0,l.g2)("q-item-section"),u=(0,l.g2)("q-item"),c=(0,l.g2)("q-item-label"),m=(0,l.g2)("q-icon"),h=(0,l.g2)("q-list");return(0,l.uX)(),(0,l.Wv)(d,null,{default:(0,l.k6)((()=>[void 0!==e.model?((0,l.uX)(),(0,l.Wv)(h,{key:0},{default:(0,l.k6)((()=>[(0,l.bF)(u,{tag:"label"},{default:(0,l.k6)((()=>[(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.bF)(r,{modelValue:a.selectedProjectsModel,"onUpdate:modelValue":t[0]||(t[0]=e=>a.selectedProjectsModel=e),options:n.projects,"option-value":"id",for:"id","option-label":"title",label:"Projects","stack-label":"",borderless:"",dense:"","options-dense":"","options-cover":"",multiple:s.multiTimeseries,"transition-show":"scale","transition-hide":"scale",clearable:""},null,8,["modelValue","options","multiple"])])),_:1})])),_:1}),n.selectedProjects.length>0?((0,l.uX)(),(0,l.CE)(l.FK,{key:0},[(0,l.bF)(u,{tag:"label"},{default:(0,l.k6)((()=>[(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.bF)(r,{modelValue:a.selectedGroupsModel,"onUpdate:modelValue":t[1]||(t[1]=e=>a.selectedGroupsModel=e),options:n.groups,"option-value":"id",for:"id","option-label":"title",label:"Groups","stack-label":"",borderless:"",dense:"","options-dense":"","options-cover":"",multiple:s.multiTimeseries,"transition-show":"scale","transition-hide":"scale",clearable:""},null,8,["modelValue","options","multiple"])])),_:1})])),_:1}),n.selectedGroups.length>0?((0,l.uX)(),(0,l.CE)(l.FK,{key:0},[(0,l.bF)(u,{tag:"label"},{default:(0,l.k6)((()=>[(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.bF)(r,{modelValue:a.selectedPlotsModel,"onUpdate:modelValue":t[2]||(t[2]=e=>a.selectedPlotsModel=e),options:n.plots,label:"Plots","stack-label":"",borderless:"",dense:"","options-dense":"","options-cover":"",multiple:s.multiTimeseries,"transition-show":"scale","transition-hide":"scale",clearable:""},null,8,["modelValue","options","multiple"]),1===n.plotstations.length&&1===n.selectedPlots.length&&n.plotstations[0].id!==n.selectedPlots[0].id?((0,l.uX)(),(0,l.Wv)(c,{key:0,caption:""},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(n.plotstations[0].id),1)])),_:1})):(0,l.Q3)("",!0)])),_:1})])),_:1}),n.selectedPlots.length>0?((0,l.uX)(),(0,l.CE)(l.FK,{key:0},[n.plotstations.length>1&&n.selectedPlotsHaveMultipleStations?((0,l.uX)(),(0,l.Wv)(u,{key:0,tag:"label"},{default:(0,l.k6)((()=>[(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.bF)(r,{modelValue:a.selectedPlotstationsModel,"onUpdate:modelValue":t[3]||(t[3]=e=>a.selectedPlotstationsModel=e),options:n.plotstations,"option-value":"id",for:"id","option-label":"id",label:"Plot-Stations","stack-label":"",borderless:"",dense:"","options-dense":"","options-cover":"",multiple:s.multiTimeseries,"transition-show":"scale","transition-hide":"scale",clearable:""},{option:(0,l.k6)((({itemProps:e,itemEvents:t,opt:s})=>[(0,l.bF)(u,(0,l.v6)(e,(0,l.Tb)(t),{class:"selection-not-active","active-class":"selection-active"}),{default:(0,l.k6)((()=>[(0,l.bF)(d,null,{default:(0,l.k6)((()=>[s.merged?((0,l.uX)(),(0,l.Wv)(c,{key:0},{default:(0,l.k6)((()=>[(0,l.Lk)("b",null,(0,o.v_)(s.plot),1),(0,l.eW)(" (merged)")])),_:2},1024)):s.plot===s.station?((0,l.uX)(),(0,l.Wv)(c,{key:1},{default:(0,l.k6)((()=>[(0,l.Lk)("b",null,(0,o.v_)(s.plot),1)])),_:2},1024)):((0,l.uX)(),(0,l.Wv)(c,{key:2},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(s.plot)+"   ",1),(0,l.Lk)("b",null,(0,o.v_)(s.station),1)])),_:2},1024))])),_:2},1024)])),_:2},1040)])),_:1},8,["modelValue","options","multiple"])])),_:1})])),_:1})):(0,l.Q3)("",!0),n.selectedPlotstations.length>0?((0,l.uX)(),(0,l.CE)(l.FK,{key:1},[(0,l.bF)(u,{tag:"label"},{default:(0,l.k6)((()=>[n.sensors.length>0?((0,l.uX)(),(0,l.Wv)(d,{key:0},{default:(0,l.k6)((()=>[(0,l.bF)(r,{modelValue:a.selectedSensorsModel,"onUpdate:modelValue":t[4]||(t[4]=e=>a.selectedSensorsModel=e),options:n.sensors,"option-value":"id",for:"id","option-label":"id",label:"Sensors","stack-label":"",borderless:"",dense:"","options-dense":"","options-cover":"",multiple:s.multiTimeseries,"transition-show":"scale","transition-hide":"scale",clearable:""},{option:(0,l.k6)((e=>[(0,l.bF)(u,(0,l.v6)(e.itemProps,{title:void 0===e.opt?"?":e.opt.description,disable:void 0===e.opt||"none"!==s.timeAggregation&&"none"===e.opt.aggregation_hour||"none"!==s.timeAggregation&&"none"===e.opt.aggregation_day||"none"!==s.timeAggregation&&"none"===e.opt.aggregation_week||"none"!==s.timeAggregation&&"none"===e.opt.aggregation_month||"none"!==s.timeAggregation&&"none"===e.opt.aggregation_year}),{default:(0,l.k6)((()=>[void 0!==e.opt?((0,l.uX)(),(0,l.Wv)(d,{key:0},{default:(0,l.k6)((()=>["none"!==e.opt.aggregation_hour?((0,l.uX)(),(0,l.Wv)(c,{key:0,class:(0,o.C4)(e.selected?"":e.opt.derived?"text-teal-10":"text-black")},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(e.opt.id),1)])),_:2},1032,["class"])):"none"!==e.opt.aggregation_day?((0,l.uX)(),(0,l.Wv)(c,{key:1,class:(0,o.C4)(e.selected?"":e.opt.derived?"text-teal-10":"text-black")},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(e.opt.id)+" (hour)",1)])),_:2},1032,["class"])):"none"!==e.opt.aggregation_week?((0,l.uX)(),(0,l.Wv)(c,{key:2,class:(0,o.C4)(e.selected?"":e.opt.derived?"text-teal-10":"text-black")},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(e.opt.id)+" (day)",1)])),_:2},1032,["class"])):"none"!==e.opt.aggregation_month?((0,l.uX)(),(0,l.Wv)(c,{key:3,class:(0,o.C4)(e.selected?"":e.opt.derived?"text-teal-10":"text-black")},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(e.opt.id)+" (day)",1)])),_:2},1032,["class"])):"none"!==e.opt.aggregation_year?((0,l.uX)(),(0,l.Wv)(c,{key:4,class:(0,o.C4)(e.selected?"":e.opt.derived?"text-teal-10":"text-black")},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(e.opt.id)+" (month)",1)])),_:2},1032,["class"])):((0,l.uX)(),(0,l.Wv)(c,{key:5,class:(0,o.C4)(e.selected?"":e.opt.derived?"text-amber-10":"text-deep-orange-10")},{default:(0,l.k6)((()=>[(0,l.eW)((0,o.v_)(e.opt.id)+" (raw)",1)])),_:2},1032,["class"]))])),_:2},1024)):(0,l.Q3)("",!0)])),_:2},1040,["title","disable"])])),_:1},8,["modelValue","options","multiple"])])),_:1})):(0,l.Q3)("",!0),0===n.sensors.length?((0,l.uX)(),(0,l.Wv)(d,{key:1,avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(m,{name:"info",color:"blue-14"})])),_:1})):(0,l.Q3)("",!0),0===n.sensors.length?((0,l.uX)(),(0,l.Wv)(d,{key:2},{default:(0,l.k6)((()=>[(0,l.eW)("No sensors available for selected plots / stations.")])),_:1})):(0,l.Q3)("",!0)])),_:1}),n.selectedSensors.length>0?((0,l.uX)(),(0,l.CE)(l.FK,{key:0},[],64)):0!==n.sensors.length?((0,l.uX)(),(0,l.Wv)(u,{key:1},{default:(0,l.k6)((()=>[(0,l.bF)(d,{avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(m,{name:"error_outline",color:"red-14"})])),_:1}),(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.eW)("No sensor selected.")])),_:1})])),_:1})):(0,l.Q3)("",!0)],64)):((0,l.uX)(),(0,l.Wv)(u,{key:2},{default:(0,l.k6)((()=>[(0,l.bF)(d,{avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(m,{name:"error_outline",color:"red-14"})])),_:1}),(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.eW)("No plot-station selected.")])),_:1})])),_:1}))],64)):((0,l.uX)(),(0,l.Wv)(u,{key:1},{default:(0,l.k6)((()=>[(0,l.bF)(d,{avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(m,{name:"error_outline",color:"red-14"})])),_:1}),(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.eW)("No plot selected.")])),_:1})])),_:1}))],64)):((0,l.uX)(),(0,l.Wv)(u,{key:1},{default:(0,l.k6)((()=>[(0,l.bF)(d,{avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(m,{name:"error_outline",color:"red-14"})])),_:1}),(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.eW)("No group selected.")])),_:1})])),_:1}))],64)):((0,l.uX)(),(0,l.Wv)(u,{key:1},{default:(0,l.k6)((()=>[(0,l.bF)(d,{avatar:""},{default:(0,l.k6)((()=>[(0,l.bF)(m,{name:"error_outline",color:"red-14"})])),_:1}),(0,l.bF)(d,null,{default:(0,l.k6)((()=>[(0,l.eW)("No project selected.")])),_:1})])),_:1}))])),_:1})):(0,l.Q3)("",!0)])),_:1})}s(8812),s(2042),s(9875),s(2090),s(8441),s(8462),s(2336);const w={name:"timeseries-selector",props:["multiTimeseries","timeAggregation"],data(){return{selectedProjectsModel:null,selectedGroupsModel:null,selectedPlotsModel:null,selectedPlotstationsModel:null,selectedSensorsModel:null}},computed:{...(0,S.aH)({model:e=>e.model.data,modelLoading:e=>e.model.loading,modelError:e=>e.model.error}),...(0,S.L8)({api:"api",apiGET:"apiGET",apiPOST:"apiPOST"}),selectedProjects(){return null===this.selectedProjectsModel?[]:this.multiTimeseries?this.selectedProjectsModel:[this.selectedProjectsModel]},selectedGroups(){return null===this.selectedGroupsModel?[]:this.multiTimeseries?this.selectedGroupsModel:[this.selectedGroupsModel]},selectedPlots(){return null===this.selectedPlotsModel?[]:this.multiTimeseries?this.selectedPlotsModel.map((e=>this.model.plots[e])):[this.model.plots[this.selectedPlotsModel]]},selectedPlotstations(){return this.selectedPlotsHaveMultipleStations?null===this.selectedPlotstationsModel?0===this.plotstations.length?[]:[this.plotstations[0]]:this.multiTimeseries?this.selectedPlotstationsModel:[this.selectedPlotstationsModel]:this.selectedPlots.map((e=>this.plotstations.find((t=>t.plot===e.id))))},selectedSensors(){return null===this.selectedSensorsModel?[]:this.multiTimeseries?this.selectedSensorsModel:[this.selectedSensorsModel]},projects(){if(void 0===this.model)return[];let e=Object.values(this.model.projects);return e.sort(((e,t)=>{const s=e.title.toLowerCase(),l=t.title.toLowerCase();return s<l?-1:s>l?1:0})),e},groups(){if(void 0===this.model||0===this.selectedProjects.length)return[];let e=[];for(let t of this.selectedProjects){let s=t.groups.map((e=>this.model.groups[e]));e=e.concat(s)}return e.sort(((e,t)=>{const s=e.title.toLowerCase(),l=t.title.toLowerCase();return s<l?-1:s>l?1:0})),e},plots(){if(void 0===this.model||0===this.selectedGroups.length)return[];let e=new Set;for(let s of this.selectedGroups)for(let t of s.plots)e.add(t);let t=[...e];return t.sort(((e,t)=>{const s=e.toLowerCase(),l=t.toLowerCase();return s<l?-1:s>l?1:0})),t},selectedPlotsHaveMultipleStations(){return this.selectedPlots.some((e=>e.stations.length>1))},plotstations(){return this.selectedPlots.flatMap((e=>e.plotstations))},sensors(){if(void 0===this.model||void 0===this.selectedPlotstations||0===this.selectedPlotstations.length)return[];let e=new Set;for(let s of this.selectedPlotstations)for(let t of s.sensorSet)e.add(t);let t=[...e].map((e=>this.model.sensors[e]));return t.sort(((e,t)=>{const s=e.id.toLowerCase(),l=t.id.toLowerCase();return s<l?-1:s>l?1:0})),t},view_time_range_limit(){let e=2e9,t=-2e9;if(this.selectedGroupsModel)for(const s of this.selectedGroupsModel)s.view_timestamp_start&&s.view_timestamp_start<e&&(e=s.view_timestamp_start),s.view_timestamp_end&&s.view_timestamp_end>t&&(t=s.view_timestamp_end);return 2e9===e&&(e=-2e9),-2e9===t&&(t=2e9),[e,t]}},methods:{onPlotSensorChanged(){this.$emit("plot-sensor-changed",{plots:this.selectedPlotstations,sensors:this.selectedSensors})}},watch:{projects:{handler(){this.selectedProjectsModel=null,1===this.projects.length&&(this.multiTimeseries?this.selectedProjectsModel=[this.projects[0]]:this.selectedProjectsModel=this.projects[0])},immediate:!0},groups:{handler(){0===this.groups.length?this.selectedGroupsModel=null:1===this.groups.length?this.multiTimeseries?this.selectedGroupsModel=[this.groups[0]]:this.selectedGroupsModel=this.groups[0]:null!==this.selectedGroupsModel&&(this.multiTimeseries?(this.selectedGroupsModel=this.selectedGroupsModel.filter((e=>this.groups.some((t=>t.id===e.id)))),0===this.selectedGroupsModel.length&&(this.selectedGroupsModel=null)):this.selectedGroupsModel=this.groups.some((e=>e.id===this.selectedGroupsModel.id))?this.selectedGroupsModel:null)},immediate:!0},plots:{handler(){0===this.plots.length?this.selectedPlotsModel=null:1===this.plots.length?this.multiTimeseries?this.selectedPlotsModel=[this.plots[0]]:this.selectedPlotsModel=this.plots[0]:null!==this.selectedPlotsModel&&(this.multiTimeseries?(this.selectedPlotsModel=this.selectedPlotsModel.filter((e=>this.plots.some((t=>t===e)))),0===this.selectedPlotsModel.length&&(this.selectedPlotsModel=null)):this.selectedPlotsModel=this.plots.some((e=>e===this.selectedPlotsModel))?this.selectedPlotsModel:null)},immediate:!0},sensors:{handler(){0===this.sensors.length?this.selectedSensorsModel=null:1===this.sensors.length?this.multiTimeseries?this.selectedSensorsModel=[this.sensors[0]]:this.selectedSensorsModel=this.sensors[0]:null!==this.selectedSensorsModel&&(this.multiTimeseries?(this.selectedSensorsModel=this.selectedSensorsModel.filter((e=>this.sensors.some((t=>t.id===e.id)))),0===this.selectedSensorsModel.length&&(this.selectedSensorsModel=null)):this.selectedSensorsModel=this.sensors.some((e=>e.id===this.selectedSensorsModel.id))?this.selectedSensorsModel:null)},immediate:!0},selectedPlots:{handler(e,t){let s=e.filter((e=>void 0===t||t.some((t=>e.id===t.id)))),l=e.filter((e=>void 0===t||!t.some((t=>e.id===t.id))));null!==this.selectedPlotstationsModel&&(l=this.multiTimeseries?l.filter((e=>!this.selectedPlotstationsModel.some((t=>t.plot===e.id)))):l.filter((e=>this.selectedPlotstationsModel.plot!==e.id)));let o=l.map((e=>this.plotstations.find((t=>t.plot===e.id)))),i=[];null!==this.selectedPlotstationsModel&&(this.multiTimeseries?i=this.selectedPlotstationsModel.filter((e=>s.some((t=>e.plot===t.id)))):s.some((e=>this.selectedPlotstationsModel.plot===e.id))&&(i=[this.selectedPlotstationsModel])),i=i.concat(o),i.length>0?this.multiTimeseries?this.selectedPlotstationsModel=i:this.selectedPlotstationsModel=i[0]:this.selectedPlotstationsModel=null},immediate:!0},selectedPlotstations(){this.$nextTick((()=>this.onPlotSensorChanged()))},selectedSensors(){this.$nextTick((()=>this.onPlotSensorChanged()))},timeAggregation(e,t){"none"===t&&"none"!==e&&null!==this.selectedSensorsModel&&(this.multiTimeseries?this.selectedSensorsModel=this.selectedSensorsModel.filter((e=>"none"!==e.aggregation_hour||"none"!==e.aggregation_day||"none"!==e.aggregation_week||"none"!==e.aggregation_month||"none"!==e.aggregation_year)):"none"!==this.selectedSensorsModel.aggregation_hour&&"none"!==this.selectedSensorsModel.aggregation_day&&"none"!==this.selectedSensorsModel.aggregation_week&&"none"!==this.selectedSensorsModel.aggregation_month&&"none"!==this.selectedSensorsModel.aggregation_year||(this.selectedSensorsModel=null))}},async mounted(){this.$store.dispatch("model/init")}};var P=s(2807),A=s(5173),q=s(3999),W=s(124),L=s(595),C=s(3796),Q=s(492),E=s(8582),X=s.n(E);const V=(0,P.A)(w,[["render",T],["__scopeId","data-v-1d7b5cbc"]]),D=V;X()(w,"components",{QItemSection:A.A,QList:q.A,QItem:W.A,QSelect:L.A,QItemLabel:C.A,QIcon:Q.A});const G={ref:"diagram"};function R(e,t,s,o,i,a){const n=(0,l.g2)("q-resize-observer");return(0,l.uX)(),(0,l.CE)("div",G,[(0,l.bF)(n,{onResize:a.onChangeDiagramDimensions,debounce:"250"},null,8,["onResize"])],512)}var j=s(8e3);function I(e){let t,s,l;function o(e){let o=e.root.querySelector(".u-over");function i(){let o=e.posToVal(e.cursor.left,"x");t=o,s=e.scales.x.min,l=e.scales.x.max}function a(){t=void 0}o.addEventListener("mousedown",(function(e){i()})),o.addEventListener("mouseup",(function(e){a()})),o.addEventListener("mousemove",(function(o){if(1===o.buttons)if(void 0===t)i();else{let o=e.posToVal(e.cursor.left,"x"),i=t-o,a=s+i,n=l+i;e.batch((()=>{e.setScale("x",{min:a,max:n})})),s=a,l=n}}))}return{hooks:{ready:o}}}function $(e){function t(e,t,s){let l,o,i,a,n,r=e.root.querySelector(".u-over"),d={x:0,y:0,dx:0,dy:0},u={x:0,y:0,dx:0,dy:0};function c(e,t){let s=t.touches,o=s[0],i=o.clientX-l.left,a=o.clientY-l.top;if(1===s.length)e.x=i,e.y=a,e.d=e.dx=e.dy=1;else{let s=t.touches[1],o=s.clientX-l.left,n=s.clientY-l.top,r=Math.min(i,o),d=Math.min(a,n),u=Math.max(i,o),c=Math.max(a,n);e.y=(d+c)/2,e.x=(r+u)/2,e.dx=u-r,e.dy=c-d,e.d=Math.sqrt(e.dx*e.dx+e.dy*e.dy)}}let m=!1;function h(){m=!1;let t=u.x,s=u.y,r=d.d/u.d,c=d.d/u.d,h=t/l.width,g=1-s/l.height,p=o*r,v=a-h*p,f=v+p,b=i*c,k=n-g*b,_=k+b;e.batch((()=>{e.setScale("x",{min:v,max:f}),e.setScale("y",{min:k,max:_})}))}function g(e){c(u,e),m||(m=!0,requestAnimationFrame(h))}r.addEventListener("touchstart",(function(t){l=r.getBoundingClientRect(),c(d,t),o=e.scales.x.max-e.scales.x.min,i=e.scales.y.max-e.scales.y.min;let s=d.x,u=d.y;a=e.posToVal(s,"x"),n=e.posToVal(u,"y"),document.addEventListener("touchmove",g,{passive:!0})})),r.addEventListener("touchend",(function(e){document.removeEventListener("touchmove",g,{passive:!0})}))}return{hooks:{init:t}}}function H(e){let t,s,l,o,i,a,n=e.factor||.75;function r(e,t,s,l,o,i){return e>l?(t=o,s=i):t<o?(t=o,s=o+e):s>i&&(s=i,t=i-e),[t,s]}return{hooks:{ready:e=>{t=e.scales.x.min,s=e.scales.x.max,l=e.scales.y.min,o=e.scales.y.max,i=s-t,a=o-l;let d=e.root.querySelector(".u-over"),u=d.getBoundingClientRect();d.addEventListener("mousedown",(t=>{if(1===t.button){t.preventDefault();let s=t.clientX,l=e.scales.x.min,o=e.scales.x.max,i=e.posToVal(1,"x")-e.posToVal(0,"x");function a(t){t.preventDefault();let a=t.clientX,n=i*(a-s);e.setScale("x",{min:l-n,max:o-n})}function n(e){document.removeEventListener("mousemove",a),document.removeEventListener("mouseup",n)}document.addEventListener("mousemove",a),document.addEventListener("mouseup",n)}})),d.addEventListener("wheel",(d=>{d.preventDefault();let{left:c,top:m}=e.cursor,h=c/u.width,g=1-m/u.height,p=e.posToVal(c,"x"),v=e.posToVal(m,"y"),f=e.scales.x.max-e.scales.x.min,b=e.scales.y.max-e.scales.y.min,k=d.deltaY<0?f*n:f/n,_=p-h*k,y=_+k;[_,y]=r(k,_,y,i,t,s);let M=d.deltaY<0?b*n:b/n,x=v-g*M,S=x+M;[x,S]=r(M,x,S,a,l,o),e.batch((()=>{e.setScale("x",{min:_,max:y})}))}))}}}}const U={name:"timeseries-diagram",props:["data","timeAggregation","highQualityDiagram"],components:{},data(){return{uplot:void 0,timeseriesStrokes:["black","red","lime","blue","gray","cyan","magenta","maroon","olive","green","purple","teal"]}},computed:{...(0,S.aH)({}),...(0,S.L8)({})},methods:{onChangeDiagramDimensions(){if(void 0!==this.uplot&&void 0!==this.$refs.diagram){const e=this.$refs.diagram.clientWidth,t=600;this.uplot.setSize({width:e,height:t})}},createDiagram(){if(void 0!==this.uplot&&(this.uplot.destroy(),this.uplot=void 0),void 0===this.data)return;const e=this.$refs.diagram.clientWidth,t=600;let s=[{}];if(this.data)for(let o=0;o<this.data.length-1;o++){let e={show:!0,spanGaps:"none"===this.timeAggregation,label:"["+(o+1)+"]",value:(e,t)=>null===t?"---":t.toFixed(2),stroke:this.timeseriesStrokes[o],width:1};this.highQualityDiagram?(e.paths=j.A.paths.spline(),e.pxAlign=0):e.paths=j.A.paths.linear(),s.push(e)}let l={width:e,height:t,cursor:{x:!1,y:!1,drag:{x:!1,y:!1}},plugins:[$({}),H({factor:.75}),I({})],series:s};this.highQualityDiagram&&(l.pxSnap=!1,l.pxAlign=0),this.$refs.diagram.innerHTML="",this.uplot=new j.A(l,this.data,this.$refs.diagram)}},watch:{data(){this.createDiagram()},highQualityDiagram(){this.createDiagram()}},async mounted(){}};var z=s(1096);const O=(0,P.A)(U,[["render",R]]),K=O;function N(e){let t=[];for(let s=0;s<e.length;s++){let l=e[s];t[s]=Number.isFinite(l)?l:null}return t}function B(e){let t=[];for(let s=0;s<e.length;s++){let l=e[s];t[s]=60*(l-36819360-60)}return t}X()(U,"components",{QResizeObserver:z.A});const Y={name:"DiagramLayout",components:{pagesToolbar:F.A,timeseriesSelector:D,timeseriesDiagram:K},data(){return{drawerWidth:400,data:void 0,dataRequestSentCounter:0,dataRequestReceivedCounter:0,dataRequestError:"no data loaded",max_timeseries:12,startTime:void 0,endTime:void 0,timeAggregation:"hour",quality:"step",interpolation:!1,highQualityDiagram:!1,selectedPlots:[],selectedSensors:[],multiTimeseries:!0}},computed:{...(0,S.aH)({model:e=>e.model.data,modelLoading:e=>e.model.loading,modelError:e=>e.model.error}),...(0,S.L8)({api:"api",apiGET:"apiGET",apiPOST:"apiPOST"}),qualities(){return["none","physical","step","empirical"]},plotSensorList(){let e=[];for(let t of this.selectedSensors)for(let s of this.selectedPlots)if(s.sensorSet.has(t.id)){let l={plot:s.id,sensor:t.id};if(e.push(l),e.length>=this.max_timeseries)return e}return e},validStartTime(){return void 0!==this.startTime&&null!==this.startTime&&this.startTime.trim().length>0},validEndTime(){return void 0!==this.endTime&&null!==this.endTime&&this.endTime.trim().length>0}},methods:{onChangeDrawerWidth(e){const t=e.delta.x;this.drawerWidth+=t,this.drawerWidth<30&&(this.drawerWidth=30),this.drawerWidth>800&&(this.drawerWidth=800)},settingsChanged(){this.requestData()},async requestData(){if(void 0!==this.model){if(this.plotSensorList.length<1)return void(this.data=void 0);this.dataRequestSentCounter++;let s=this.dataRequestSentCounter;try{this.dataRequestError=void 0;let e={responseType:"arraybuffer"},t={timeAggregation:this.timeAggregation,quality:this.quality};this.validStartTime?t.start_time=this.startTime:this.validEndTime&&(t.start_time=this.endTime),this.validEndTime?t.end_time=this.endTime:this.validStartTime&&(t.end_time=this.startTime),"none"!==this.timeAggregation&&(t.interpolation=this.interpolation),this.$refs.timeseriesSelector.view_time_range_limit&&(t.view_time_limit_start=this.$refs.timeseriesSelector.view_time_range_limit[0],t.view_time_limit_end=this.$refs.timeseriesSelector.view_time_range_limit[1]);let l={settings:t,timeseries:this.plotSensorList};const o=await this.apiPOST(["tsdb","query_js"],l,e);if(s<this.dataRequestSentCounter)return;this.dataRequestReceivedCounter=s;let i=o.data,a=new DataView(i),n=a.getInt32(0,!0),r=a.getInt32(4,!0),d=[],u=new Int32Array(i,8,n);d[0]=B(u);for(let s=0;s<r;s++){let e=new Float32Array(i,8+4*n*(s+1),n);d[s+1]=N(e)}this.data=d}catch(e){if(console.log(e),s<this.dataRequestSentCounter)return;if(this.dataRequestError="ERROR receiving data: "+e,e.response&&e.response.data)try{const t=new TextDecoder("utf-8").decode(e.response.data);this.dataRequestError+=" ::  "+t}catch(t){console.log(t)}this.dataRequestReceivedCounter=s,this.data=void 0}}}},watch:{async model(){this.requestData()},startTime(){this.settingsChanged()},endTime(){this.settingsChanged()},timeAggregation:{handler(){(void 0===this.quality||null===this.quality||"none"===this.timeAggregation&&"empirical"===this.quality)&&(this.quality=this.qualities[2]),this.settingsChanged()},immediate:!0},quality(){this.settingsChanged()},interpolation(){this.settingsChanged()},plotSensorList(){this.settingsChanged()}},async mounted(){this.requestData(),this.$store.dispatch("model/init")}};var Z=s(2008),J=s(8416),ee=s(6056),te=s(9383),se=s(9270),le=s(849),oe=s(386),ie=s(2157),ae=s(1693),ne=s(5205),re=s(9815);const de=(0,P.A)(Y,[["render",x],["__scopeId","data-v-72f5beff"]]),ue=de;X()(Y,"components",{QLayout:Z.A,QHeader:J.A,QDrawer:ee.A,QScrollArea:te.A,QList:q.A,QItem:W.A,QItemSection:A.A,QInput:se.A,QSelect:L.A,QCheckbox:le.A,QItemLabel:C.A,QSeparator:oe.A,QSpinnerIos:ie.A,QBtn:ae.A,QPageContainer:ne.A,QIcon:Q.A}),X()(Y,"directives",{TouchPan:re.A})}}]);