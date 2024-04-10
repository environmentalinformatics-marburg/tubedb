"use strict";(globalThis["webpackChunkapp"]=globalThis["webpackChunkapp"]||[]).push([[676],{2914:(e,s,t)=>{t.d(s,{A:()=>u});var o=t(1758),r=t(8790);function n(e,s,t,n,_,a){const E=(0,o.g2)("pages-menu"),l=(0,o.g2)("q-space"),W=(0,o.g2)("q-toolbar-title"),i=(0,o.g2)("q-toolbar");return(0,o.uX)(),(0,o.Wv)(i,{class:"bg-grey-1 text-grey-8"},{default:(0,o.k6)((()=>[(0,o.bF)(E,{active:t.active},null,8,["active"]),(0,o.bF)(l),(0,o.bF)(W,{class:"title"},{default:(0,o.k6)((()=>[(0,o.eW)((0,r.v_)(t.title),1)])),_:1}),(0,o.bF)(l)])),_:1})}function _(e,s,t,n,_,a){const E=(0,o.g2)("q-item-section"),l=(0,o.g2)("q-item"),W=(0,o.g2)("q-list"),i=(0,o.g2)("q-menu"),w=(0,o.g2)("q-btn");return(0,o.uX)(),(0,o.Wv)(w,{flat:"",round:"",dense:"",icon:"menu"},{default:(0,o.k6)((()=>[(0,o.bF)(i,{"transition-show":"scale","transition-hide":"scale",class:"bg-grey-1 text-grey-8"},{default:(0,o.k6)((()=>[(0,o.bF)(W,{style:{"min-width":"100px"}},{default:(0,o.k6)((()=>[((0,o.uX)(!0),(0,o.CE)(o.FK,null,(0,o.pI)(_.items,(e=>((0,o.uX)(),(0,o.Wv)(l,{key:e.title,to:e.link,clickable:"",class:(0,r.C4)({activeitem:t.active===e.link})},{default:(0,o.k6)((()=>[(0,o.bF)(E,null,{default:(0,o.k6)((()=>[(0,o.eW)((0,r.v_)(e.title),1)])),_:2},1024)])),_:2},1032,["to","class"])))),128))])),_:1})])),_:1})])),_:1})}const a={name:"pages-menu",props:["active"],data(){return{items:[{title:"Overview",link:"/overview"},{title:"Metadata",link:"/model"},{title:"Diagrams",link:"/diagram"},{title:"Monitoring",link:"/monitoring"},{title:"Plot status",link:"/plot_status"}]}},computed:{},methods:{},watch:{},async mounted(){}};var E=t(2807),l=t(7954),W=t(6929),i=t(4514),w=t(5329),S=t(3418),d=t(8582),k=t.n(d);const T=(0,E.A)(a,[["render",_],["__scopeId","data-v-003556f6"]]),m=T;k()(a,"components",{QBtn:l.A,QMenu:W.A,QList:i.A,QItem:w.A,QItemSection:S.A});const c={name:"pages-toolbar",props:["title","active"],components:{pagesMenu:m},data(){return{}},computed:{},methods:{},watch:{},async mounted(){}};var p=t(9351),A=t(1173),H=t(6769);const G=(0,E.A)(c,[["render",n],["__scopeId","data-v-1fb16e4f"]]),u=G;k()(c,"components",{QToolbar:p.A,QSpace:A.A,QToolbarTitle:H.A})},6295:(e,s,t)=>{t.r(s),t.d(s,{default:()=>b});var o=t(1758),r=t(8790);const n={key:0},_={key:1},a={key:2,style:{color:"red","padding-top":"20px","font-size":"1.5em"}},E={key:3,style:{color:"red","padding-top":"20px","font-size":"1.5em"}};function l(e,s,t,l,W,i){const w=(0,o.g2)("pages-toolbar"),S=(0,o.g2)("q-header"),d=(0,o.g2)("q-select"),k=(0,o.g2)("q-checkbox"),T=(0,o.g2)("q-toolbar"),m=(0,o.g2)("q-btn"),c=(0,o.g2)("q-td"),p=(0,o.g2)("q-tr"),A=(0,o.g2)("q-table"),H=(0,o.g2)("q-icon"),G=(0,o.g2)("q-page"),u=(0,o.g2)("q-page-container"),g=(0,o.g2)("q-layout");return(0,o.uX)(),(0,o.Wv)(g,{view:"hHh LpR fFf"},{default:(0,o.k6)((()=>[(0,o.bF)(S,{reveal:"",elevated:"",class:"bg-grey-7 text-grey-4"},{default:(0,o.k6)((()=>[(0,o.bF)(w,{title:"TubeDB monitoring",active:"/monitoring"})])),_:1}),(0,o.bF)(u,{class:"row"},{default:(0,o.k6)((()=>[(0,o.bF)(G,{padding:"",class:"column"},{default:(0,o.k6)((()=>[(0,o.bF)(T,{class:"shadow-2"},{default:(0,o.k6)((()=>[(0,o.bF)(d,{outlined:"",label:"Select monitored set",options:i.sets,"option-label":"set","stack-label":"",modelValue:W.selectedSet,"onUpdate:modelValue":s[0]||(s[0]=e=>W.selectedSet=e),"display-value":W.selectedSet?W.selectedSet.set:"* no set selected *","options-dense":"",dense:"",style:{width:"250px"},title:"Choose one predefined monitoring set containing plots, sensors and monitoring settings."},null,8,["options","modelValue","display-value"]),void 0!==W.selectedSet?((0,o.uX)(),(0,o.Wv)(d,{key:0,outlined:"",label:"Select monitored plots",options:W.selectedSet.plots,"stack-label":"",modelValue:W.selectedPlots,"onUpdate:modelValue":s[1]||(s[1]=e=>W.selectedPlots=e),"display-value":W.selectedPlots?W.selectedPlots.join(", "):"* all plots *",multiple:"",clearable:"","options-dense":"",dense:"",style:{width:"250px"},title:"Leave empty to select all plots."},null,8,["options","modelValue","display-value"])):(0,o.Q3)("",!0),void 0!==W.selectedSet?((0,o.uX)(),(0,o.Wv)(d,{key:1,outlined:"",label:"Select monitored sensors",options:W.selectedSet.sensors,"stack-label":"","option-label":"sensor",modelValue:W.selectedSensors,"onUpdate:modelValue":s[2]||(s[2]=e=>W.selectedSensors=e),"display-value":W.selectedSensors?W.selectedSensors.map((e=>e.sensor)).join(", "):"* all sensors *",multiple:"",clearable:"","options-dense":"",dense:"",style:{width:"250px"},title:"Leave empty to select all sensors."},null,8,["options","modelValue","display-value"])):(0,o.Q3)("",!0),void 0!==W.selectedSet?((0,o.uX)(),(0,o.Wv)(k,{key:2,size:"xs",modelValue:W.showAllTimestamps,"onUpdate:modelValue":s[3]||(s[3]=e=>W.showAllTimestamps=e),label:"show timestamps for sensors"},null,8,["modelValue"])):(0,o.Q3)("",!0)])),_:1}),void 0!==W.selectedSet?((0,o.uX)(),(0,o.Wv)(m,{key:0,onClick:i.refresh,loading:W.dataLoading,icon:"refresh"},{default:(0,o.k6)((()=>[(0,o.eW)("refresh")])),_:1},8,["onClick","loading"])):(0,o.Q3)("",!0),void 0!==W.selectedSet?((0,o.uX)(),(0,o.Wv)(A,{key:1,dense:"",columns:i.columns,rows:i.rows,"row-key":"plot","rows-per-page-options":[0],pagination:W.pagination,"hide-pagination":"","sort-method":i.customSort,"binary-state-sort":""},{body:(0,o.k6)((e=>[(0,o.bF)(p,{props:e},{default:(0,o.k6)((()=>[(0,o.bF)(c,{key:"plot",props:e,class:(0,r.C4)(i.plotClass(e.row))},{default:(0,o.k6)((()=>[(0,o.Lk)("b",null,(0,r.v_)(e.row.plot),1)])),_:2},1032,["props","class"]),((0,o.uX)(!0),(0,o.CE)(o.FK,null,(0,o.pI)(i.sensorColumns,(s=>((0,o.uX)(),(0,o.Wv)(c,{props:e,key:s.sensor,class:(0,r.C4)(i.cellClass(s,e.row)),title:e.row.plot+" "+s.sensor},{default:(0,o.k6)((()=>[s.number?((0,o.uX)(),(0,o.CE)("span",n,(0,r.v_)(-99999===e.row[s.sensor]?"":e.row[s.sensor].toFixed(2)),1)):((0,o.uX)(),(0,o.CE)("i",_,(0,r.v_)(0===e.row[s.sensorTimestamp]?"":e.row[s.sensor]),1))])),_:2},1032,["props","class","title"])))),128))])),_:2},1032,["props"])])),_:1},8,["columns","rows","pagination","sort-method"])):(0,o.Q3)("",!0),void 0===W.selectedSet?((0,o.uX)(),(0,o.CE)("div",a,[(0,o.bF)(H,{name:"event_note"}),(0,o.eW)(" Select a monitoring set! ")])):(0,o.Q3)("",!0),void 0!==W.selectedSet&&void 0===W.data?((0,o.uX)(),(0,o.CE)("div",E,[(0,o.bF)(H,{name:"event_note"}),(0,o.eW)(" Click refresh button to load data! ")])):(0,o.Q3)("",!0)])),_:1})])),_:1})])),_:1})}t(239),t(7396),t(923),t(9502);var W=t(6980),i=t(2914);const w={components:{pagesToolbar:i.A},data(){return{data:void 0,dataLoading:!1,pagination:{page:1,rowsPerPage:0},showAllTimestamps:!1,monitoring_meta:{sets:[{set:"Exploratories AEG",plots:["AEG01","AEG02","AEG03","AEG04","AEG05","AEG06","AEG07","AEG08","AEG09","AEG10","AEG11","AEG12","AEG13","AEG14","AEG15","AEG16","AEG17","AEG18","AEG19","AEG20","AEG21","AEG22","AEG23","AEG24","AEG25","AEG26","AEG27","AEG28","AEG29","AEG30","AEG31","AEG32","AEG33","AEG34","AEG35","AEG36","AEG37","AEG38","AEG39","AEG40","AEG41","AEG42","AEG43","AEG44","AEG45","AEG46","AEG47","AEG48","AEG49","AEG50"],sensors:[{sensor:"UB",ok:[12.2,14.9],warn:[11.9,14.9]},{sensor:"Ta_200",ok:[-20,35],warn:[-40,40]},{sensor:"Ta_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_05",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_20",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_50",ok:[-20,35],warn:[-40,40]},{sensor:"SM_10",ok:[1,65],warn:[0,70]},{sensor:"SM_20",ok:[1,65],warn:[0,70]},{sensor:"rH_200",ok:[15,100],warn:[0,100]},{sensor:"LWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"LWUR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWUR_300",ok:[0,1e3],warn:[0,1500]}]},{set:"Exploratories AEW",plots:["AEW01","AEW02","AEW03","AEW04","AEW05","AEW06","AEW07","AEW08","AEW09","AEW10","AEW11","AEW12","AEW13","AEW14","AEW15","AEW16","AEW17","AEW18","AEW19","AEW20","AEW21","AEW22","AEW23","AEW24","AEW25","AEW26","AEW27","AEW28","AEW29","AEW30","AEW31","AEW32","AEW33","AEW34","AEW35","AEW36","AEW37","AEW38","AEW39","AEW40","AEW41","AEW42","AEW43","AEW44","AEW45","AEW46","AEW47","AEW48","AEW49","AEW50"],sensors:[{sensor:"UB",ok:[12.2,14.9],warn:[11.9,14.9]},{sensor:"Ta_200",ok:[-20,35],warn:[-40,40]},{sensor:"Ta_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_05",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_20",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_50",ok:[-20,35],warn:[-40,40]},{sensor:"SM_10",ok:[1,65],warn:[0,70]},{sensor:"SM_20",ok:[1,65],warn:[0,70]},{sensor:"rH_200",ok:[15,100],warn:[0,100]},{sensor:"LWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"LWUR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWUR_300",ok:[0,1e3],warn:[0,1500]}]},{set:"Exploratories AEF",plots:["AEW03_1_M","AEW03_1_S","AEW03_2_M","AEW03_2_N","AEW03_3_M","AEW22_1_M","AEW22_1_N","AEW22_2_M","AEW22_2_S","AEW22_3_M","AEW28_1_M","AEW28_1_N","AEW28_2_M","AEW28_2_S","AEW28_3_M","AEW33_4_M","AEW33_1_M","AEW33_1_S","AEW33_2_M","AEW33_2_N","AEW33_3_M","AEW34_1_M","AEW34_1_N","AEW34_2_M","AEW34_2_S","AEW34_3_M","AEW39_1_M","AEW39_1_S","AEW39_2_M","AEW39_2_N","AEW39_3_M","AEW42_1_M","AEW42_1_S","AEW42_2_M","AEW42_2_N","AEW42_3_M","AEW47_1_M","AEW47_1_N","AEW47_2_M","AEW47_2_S","AEW47_3_M"],sensors:[{sensor:"tt_battery_voltage",ok:[3700,5e3],warn:[3500,5e3]},{sensor:"tt_air_temperature",ok:[-20,35],warn:[-40,40]},{sensor:"tt_air_relative_humidity",ok:[15,100],warn:[0,100]},{sensor:"ttraw_gms_fq_1",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_fq_2",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_fq_3",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_ntc_1",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"ttraw_gms_ntc_2",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"ttraw_gms_ntc_3",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"tt_gms_T_1",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_T_2",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_T_3",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_ECf_T_1",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_ECf_T_2",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_ECf_T_3",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_delta_ECf_1",ok:[-500,500],warn:[-1e3,2e3]},{sensor:"tt_gms_delta_ECf_2",ok:[-500,500],warn:[-1e3,2e3]},{sensor:"tt_gms_delta_ECf_3",ok:[-500,500],warn:[-1e3,2e3]}]},{set:"Exploratories AEF cloud",plots:["AEW03_CLOUD","AEW22_CLOUD","AEW28_CLOUD","AEW33_CLOUD","AEW34_CLOUD","AEW39_CLOUD","AEW42_CLOUD","AEW47_CLOUD"],sensors:[{sensor:"tt_Battery_level",ok:[3800,5e3],warn:[3600,5e3]},{sensor:"ttraw_GSM_field level"},{sensor:"ttraw_accumulated_records"},{sensor:"ttraw_records_to_send"},{sensor:"ttraw_TT01"},{sensor:"ttraw_TT02"},{sensor:"ttraw_TT03"},{sensor:"ttraw_TT04"},{sensor:"ttraw_TT05"},{sensor:"ttraw_TT06"},{sensor:"ttraw_TT07"},{sensor:"ttraw_TT08"},{sensor:"ttraw_TT09"},{sensor:"ttraw_TT10"},{sensor:"ttraw_TT11"},{sensor:"ttraw_TT12"},{sensor:"ttraw_TT13"},{sensor:"ttraw_TT14"},{sensor:"ttraw_TT15"},{sensor:"ttraw_TT16"},{sensor:"ttraw_TT17"},{sensor:"ttraw_TT18"},{sensor:"ttraw_TT19"},{sensor:"ttraw_TT20"},{sensor:"ttraw_TT21"}]},{set:"Exploratories HEG",plots:["HEG01","HEG02","HEG03","HEG04","HEG05","HEG06","HEG07","HEG08","HEG09","HEG10","HEG11","HEG12","HEG13","HEG14","HEG15","HEG16","HEG17","HEG18","HEG19","HEG20","HEG21","HEG22","HEG23","HEG24","HEG25","HEG26","HEG27","HEG28","HEG29","HEG30","HEG31","HEG32","HEG33","HEG34","HEG35","HEG36","HEG37","HEG38","HEG39","HEG40","HEG41","HEG42","HEG43","HEG44","HEG45","HEG46","HEG47","HEG48","HEG49","HEG50"],sensors:[{sensor:"UB",ok:[12.2,14.9],warn:[11.9,14.9]},{sensor:"Ta_200",ok:[-20,35],warn:[-40,40]},{sensor:"Ta_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_05",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_20",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_50",ok:[-20,35],warn:[-40,40]},{sensor:"SM_10",ok:[1,65],warn:[0,70]},{sensor:"SM_20",ok:[1,65],warn:[0,70]},{sensor:"rH_200",ok:[15,100],warn:[0,100]},{sensor:"LWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"LWUR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWUR_300",ok:[0,1e3],warn:[0,1500]}]},{set:"Exploratories HEW",plots:["HEW01","HEW02","HEW03","HEW04","HEW05","HEW06","HEW07","HEW08","HEW09","HEW10","HEW11","HEW12","HEW13","HEW14","HEW15","HEW16","HEW17","HEW18","HEW19","HEW20","HEW21","HEW22","HEW23","HEW24","HEW25","HEW26","HEW27","HEW28","HEW29","HEW30","HEW31","HEW32","HEW33","HEW34","HEW35","HEW36","HEW37","HEW38","HEW39","HEW40","HEW41","HEW42","HEW43","HEW44","HEW45","HEW46","HEW47","HEW48","HEW49","HEW50","HEW51"],sensors:[{sensor:"UB",ok:[12.2,14.9],warn:[11.9,14.9]},{sensor:"Ta_200",ok:[-20,35],warn:[-40,40]},{sensor:"Ta_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_05",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_20",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_50",ok:[-20,35],warn:[-40,40]},{sensor:"SM_10",ok:[1,65],warn:[0,70]},{sensor:"SM_20",ok:[1,65],warn:[0,70]},{sensor:"rH_200",ok:[15,100],warn:[0,100]},{sensor:"LWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"LWUR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWUR_300",ok:[0,1e3],warn:[0,1500]}]},{set:"Exploratories HEF",plots:["HEW05_3_M","HEW05_1_M","HEW05_1_N","HEW05_2_M","HEW05_2_S","HEW06_1_M","HEW06_1_S","HEW06_2_M","HEW06_2_N","HEW06_3_M","HEW19_1_N","HEW19_2_M","HEW19_1_M","HEW19_2_S","HEW19_3_M","HEW21_1_M","HEW21_1_S","HEW21_3_M","HEW21_2_M","HEW21_2_N","HEW29_1_M","HEW29_1_S","HEW29_2_M","HEW29_2_N","HEW29_3_M","HEW30_1_M","HEW30_1_N","HEW30_2_M","HEW30_2_S","HEW30_3_M","HEW32_2_M","HEW32_1_M","HEW32_1_S","HEW32_2_N","HEW32_3_M","HEW47_1_M","HEW47_1_N","HEW47_2_M","HEW47_2_S","HEW47_3_M","HEW48_1_M","HEW48_1_S","HEW48_2_M","HEW48_2_N","HEW48_3_M","HEW48_4_M"],sensors:[{sensor:"tt_battery_voltage",ok:[3700,5e3],warn:[3500,5e3]},{sensor:"tt_air_temperature",ok:[-20,35],warn:[-40,40]},{sensor:"tt_air_relative_humidity",ok:[15,100],warn:[0,100]},{sensor:"ttraw_gms_fq_1",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_fq_2",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_fq_3",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_ntc_1",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"ttraw_gms_ntc_2",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"ttraw_gms_ntc_3",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"tt_gms_T_1",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_T_2",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_T_3",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_ECf_T_1",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_ECf_T_2",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_ECf_T_3",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_delta_ECf_1",ok:[-500,500],warn:[-1e3,2e3]},{sensor:"tt_gms_delta_ECf_2",ok:[-500,500],warn:[-1e3,2e3]},{sensor:"tt_gms_delta_ECf_3",ok:[-500,500],warn:[-1e3,2e3]}]},{set:"Exploratories HEF cloud",plots:["HEW05_CLOUD","HEW06_CLOUD","HEW19_CLOUD","HEW21_CLOUD","HEW29_CLOUD","HEW30_CLOUD","HEW32_CLOUD","HEW47_CLOUD","HEW48_CLOUD"],sensors:[{sensor:"tt_Battery_level",ok:[3800,5e3],warn:[3600,5e3]},{sensor:"ttraw_GSM_field level"},{sensor:"ttraw_accumulated_records"},{sensor:"ttraw_records_to_send"},{sensor:"ttraw_TT01"},{sensor:"ttraw_TT02"},{sensor:"ttraw_TT03"},{sensor:"ttraw_TT04"},{sensor:"ttraw_TT05"},{sensor:"ttraw_TT06"},{sensor:"ttraw_TT07"},{sensor:"ttraw_TT08"},{sensor:"ttraw_TT09"},{sensor:"ttraw_TT10"},{sensor:"ttraw_TT11"},{sensor:"ttraw_TT12"},{sensor:"ttraw_TT13"},{sensor:"ttraw_TT14"},{sensor:"ttraw_TT15"},{sensor:"ttraw_TT16"},{sensor:"ttraw_TT17"},{sensor:"ttraw_TT18"},{sensor:"ttraw_TT19"},{sensor:"ttraw_TT20"},{sensor:"ttraw_TT21"}]},{set:"Exploratories SEG",plots:["SEG01","SEG02","SEG03","SEG04","SEG05","SEG06","SEG07","SEG08","SEG09","SEG10","SEG11","SEG12","SEG13","SEG14","SEG15","SEG16","SEG17","SEG18","SEG19","SEG20","SEG21","SEG22","SEG23","SEG24","SEG25","SEG26","SEG27","SEG28","SEG29","SEG30","SEG31","SEG32","SEG33","SEG34","SEG35","SEG36","SEG37","SEG38","SEG39","SEG40","SEG41","SEG42","SEG43","SEG44","SEG45","SEG46","SEG47","SEG48","SEG49","SEG50"],sensors:[{sensor:"UB",ok:[12.2,14.9],warn:[11.9,14.9]},{sensor:"Ta_200",ok:[-20,35],warn:[-40,40]},{sensor:"Ta_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_05",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_20",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_50",ok:[-20,35],warn:[-40,40]},{sensor:"SM_10",ok:[1,65],warn:[0,70]},{sensor:"SM_20",ok:[1,65],warn:[0,70]},{sensor:"rH_200",ok:[15,100],warn:[0,100]},{sensor:"LWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"LWUR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWUR_300",ok:[0,1e3],warn:[0,1500]}]},{set:"Exploratories SEW",plots:["SEW01","SEW02","SEW03","SEW04","SEW05","SEW06","SEW07","SEW08","SEW09","SEW10","SEW11","SEW12","SEW13","SEW14","SEW15","SEW16","SEW17","SEW18","SEW19","SEW20","SEW21","SEW22","SEW23","SEW24","SEW25","SEW26","SEW27","SEW28","SEW29","SEW30","SEW31","SEW32","SEW33","SEW34","SEW35","SEW36","SEW37","SEW38","SEW39","SEW40","SEW41","SEW42","SEW43","SEW44","SEW45","SEW46","SEW47","SEW48","SEW49","SEW50"],sensors:[{sensor:"UB",ok:[12.2,14.9],warn:[11.9,14.9]},{sensor:"Ta_200",ok:[-20,35],warn:[-40,40]},{sensor:"Ta_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_05",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_10",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_20",ok:[-20,35],warn:[-40,40]},{sensor:"Ts_50",ok:[-20,35],warn:[-40,40]},{sensor:"SM_10",ok:[1,65],warn:[0,70]},{sensor:"SM_20",ok:[1,65],warn:[0,70]},{sensor:"rH_200",ok:[15,100],warn:[0,100]},{sensor:"LWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"LWUR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWDR_300",ok:[0,1e3],warn:[0,1500]},{sensor:"SWUR_300",ok:[0,1e3],warn:[0,1500]}]},{set:"Exploratories SEF",plots:["SEW03_2_M","SEW03_1_S","SEW03_3_M","SEW03_2_N","SEW03_1_M","SEW04_1_M","SEW04_1_S","SEW04_2_M","SEW04_2_N","SEW04_3_M","SEW04_4_M","SEW17_1_M","SEW17_1_N","SEW17_2_M","SEW17_2_S","SEW17_3_M","SEW18_1_N","SEW18_1_M","SEW18_2_S","SEW18_2_M","SEW18_3_M","SEW24_3_M","SEW24_2_M","SEW24_1_S","SEW24_2_N","SEW24_1_M","SEW25_1_M","SEW25_1_S","SEW25_2_M","SEW25_2_N","SEW25_3_M","SEW27_1_M","SEW27_1_N","SEW27_2_M","SEW27_2_S","SEW31_1_M","SEW31_1_S","SEW31_2_M","SEW31_2_N","SEW31_3_M","SEW33_1_M","SEW33_1_S","SEW33_2_M","SEW33_2_N","SEW33_3_M","SEW35_3_M","SEW35_1_M","SEW35_1_N","SEW35_2_M","SEW35_2_S","SEW36_3_M","SEW36_1_M","SEW36_1_N","SEW36_2_M","SEW36_2_S","SEW49_3_M","SEW49_1_M","SEW49_1_N","SEW49_2_M","SEW49_2_S"],sensors:[{sensor:"tt_battery_voltage",ok:[3700,5e3],warn:[3500,5e3]},{sensor:"tt_air_temperature",ok:[-20,35],warn:[-40,40]},{sensor:"tt_air_relative_humidity",ok:[15,100],warn:[0,100]},{sensor:"ttraw_gms_fq_1",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_fq_2",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_fq_3",ok:[1e4,2e4],warn:[5e3,25e3]},{sensor:"ttraw_gms_ntc_1",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"ttraw_gms_ntc_2",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"ttraw_gms_ntc_3",ok:[4e4,45e3],warn:[35e3,5e4]},{sensor:"tt_gms_T_1",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_T_2",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_T_3",ok:[-20,35],warn:[-40,40]},{sensor:"tt_gms_ECf_T_1",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_ECf_T_2",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_ECf_T_3",ok:[-200,-0],warn:[-400,200]},{sensor:"tt_gms_delta_ECf_1",ok:[-500,500],warn:[-1e3,2e3]},{sensor:"tt_gms_delta_ECf_2",ok:[-500,500],warn:[-1e3,2e3]},{sensor:"tt_gms_delta_ECf_3",ok:[-500,500],warn:[-1e3,2e3]}]},{set:"Exploratories SEF cloud",plots:["SEW03_CLOUD","SEW04_CLOUD","SEW17_CLOUD","SEW18_CLOUD","SEW24_CLOUD","SEW25_CLOUD","SEW27_CLOUD","SEW31_CLOUD","SEW33_CLOUD","SEW35_CLOUD","SEW36_CLOUD","SEW49_CLOUD"],sensors:[{sensor:"tt_Battery_level",ok:[3800,5e3],warn:[3600,5e3]},{sensor:"ttraw_GSM_field level"},{sensor:"ttraw_accumulated_records"},{sensor:"ttraw_records_to_send"},{sensor:"ttraw_TT01"},{sensor:"ttraw_TT02"},{sensor:"ttraw_TT03"},{sensor:"ttraw_TT04"},{sensor:"ttraw_TT05"},{sensor:"ttraw_TT06"},{sensor:"ttraw_TT07"},{sensor:"ttraw_TT08"},{sensor:"ttraw_TT09"},{sensor:"ttraw_TT10"},{sensor:"ttraw_TT11"},{sensor:"ttraw_TT12"},{sensor:"ttraw_TT13"},{sensor:"ttraw_TT14"},{sensor:"ttraw_TT15"},{sensor:"ttraw_TT16"},{sensor:"ttraw_TT17"},{sensor:"ttraw_TT18"},{sensor:"ttraw_TT19"},{sensor:"ttraw_TT20"},{sensor:"ttraw_TT21"}]}]},selectedSet:void 0,selectedSensors:void 0,selectedPlots:void 0}},computed:{...(0,W.L8)({apiGET:"apiGET"}),columns(){let e=[{name:"plot",field:"plot",label:"Plot",sortable:!0,align:"left"}];if(void 0!==this.selectedSet&&void 0!==this.data){let s=!0;this.data.sensors.forEach((t=>{const o=this.selectedSet.sensors.find((e=>e.sensor===t)),r=o.sensor+".datetime";(this.showAllTimestamps||s)&&(s=!1,e.push({name:r,field:r,label:"Timestamp",sortable:!0})),e.push({name:o.sensor,field:o.sensor,label:o.sensor,sortable:!0})}))}return e},columnMap(){let e={plot:{sortType:"text"}};return void 0!==this.selectedSet&&this.selectedSet.sensors.forEach((s=>{const t=s.sensor+".datetime";e[s.sensor]={sortType:"number"},e[t]={sortType:"text"}})),e},sensorColumns(){let e=[];if(void 0!==this.selectedSet&&void 0!==this.data){const s=this.data.sensors,t=this.selectedSet.sensors;s.forEach((s=>{const o=t.find((e=>e.sensor===s));if(o){const s=o.sensor+".datetime",t=o.sensor+".timestamp";e.push({sensor:s,datetime:!0,number:!1,sensorTimestamp:t}),e.push({sensor:o.sensor,datetime:!1,number:!0,ok:o.ok,warn:o.warn,sensorTimestamp:t})}else console.log("sensor not found "+s)}))}return e},rows(){if(void 0===this.data)return[];const e=this.data.sensors;let s=this.data.measurements.map((s=>{let t={plot:s.plot};const o=s.datetime,r=s.value,n=s.timestamp;for(let _=0;_<e.length;_++){const s=e[_],a=s+".datetime",E=s+".timestamp";t[a]=o[_],t[s]=r[_],t[E]=n[_]}return t}));return s},timestampNow(){return void 0===this.data?0:this.data.timestamp},sets(){return void 0===this.monitoring_meta?[]:this.monitoring_meta.sets}},methods:{async refresh(){try{const e=new URLSearchParams;this.selectedPlots?this.selectedPlots.forEach((s=>e.append("plot",s))):this.selectedSet.plots.forEach((s=>e.append("plot",s))),this.selectedSensors?this.selectedSensors.forEach((s=>e.append("sensor",s.sensor))):this.selectedSet.sensors.forEach((s=>e.append("sensor",s.sensor))),this.dataLoading=!0;const s=await this.apiGET(["tsdb","monitoring"],{params:e});this.data=s.data}catch(e){this.data=void 0,console.log(e),this.$q.notify({message:"Error loading data.",type:"negative"})}finally{this.dataLoading=!1}},cellClass(e,s){const t=s[e.sensor],o=s[e.sensorTimestamp],r=this.timestampNow-o,n=r>1e5||r<-1440,_=r>2e5||r<-2880;return e.number?-99999===t?"":e.ok?e.ok[0]<=t&&t<=e.ok[1]?n?"sensor-ok-outdated":"sensor-ok":e.warn&&e.warn[0]<=t&&t<=e.warn[1]?n?"sensor-warn-outdated":"sensor-warn":n?"sensor-error-outdated":"sensor-error":e.warn?e.warn[0]<=t&&t<=e.warn[1]?n?"sensor-warn-outdated":"sensor-warn":n?"sensor-error-outdated":"sensor-error":n?"sensor-ok-outdated":"sensor-ok":e.datetime?n?_?"time-error":"time-warn":"time-ok":""},plotClass(e){let s="sensor-ok";return this.sensorColumns.forEach((t=>{const o=e[t.sensor],r=e[t.sensorTimestamp],n=this.timestampNow-r,_=n>1e5||n<-1440,a=n>2e5||n<-2880;t.number?-99999===o||(t.ok?t.ok[0]<=o&&o<=t.ok[1]||(t.warn&&t.warn[0]<=o&&o<=t.warn[1]?"sensor-ok"===s&&(s="sensor-warn"):s="sensor-error"):t.warn&&(t.warn[0]<=o&&o<=t.warn[1]?"sensor-ok"===s&&(s="sensor-warn"):s="sensor-error")):t.datetime&&(0===r||_&&(a?s="sensor-error":"sensor-ok"===s&&(s="sensor-warn")))})),s},customSort(e,s,t){const o=[...e];if(s){const e=this.columnMap[s];let r;switch(e.sortType){case"number":r=t?(e,t)=>parseFloat(t[s])-parseFloat(e[s]):(e,t)=>parseFloat(e[s])-parseFloat(t[s]);break;default:r=t?(e,t)=>t[s]>e[s]?1:t[s]<e[s]?-1:0:(e,t)=>e[s]>t[s]?1:e[s]<t[s]?-1:0}void 0!==r&&o.sort(r)}return o}},watch:{selectedSet(){this.selectedPlots=void 0,this.selectedSensors=void 0,this.data=void 0}},async mounted(){}};var S=t(2807),d=t(3333),k=t(133),T=t(1102),m=t(3177),c=t(9351),p=t(5685),A=t(790),H=t(7954),G=t(9711),u=t(1239),g=t(7109),M=t(4609),h=t(8582),f=t.n(h);const v=(0,S.A)(w,[["render",l],["__scopeId","data-v-b82f7ed4"]]),b=v;f()(w,"components",{QLayout:d.A,QHeader:k.A,QPageContainer:T.A,QPage:m.A,QToolbar:c.A,QSelect:p.A,QCheckbox:A.A,QBtn:H.A,QTable:G.A,QTr:u.A,QTd:g.A,QIcon:M.A})}}]);