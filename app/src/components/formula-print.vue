<template>
  <!--<div :class="levelClass" style="display: inline-block; text-align: center; vertical-align: middle;">-->
<div :class="[formulaOpClass, levelClass]">
  <template v-if="node.op === 'add'">
    <template v-for="(sub, index) in node.terms">
      <div :class="{'formula-op-add-column': true, 'formula-op-add-column-follow': index > 0}" :key="JSON.stringify(sub)+0">{{index == 0 && sub.positive ? '' : sub.positive ? '+' : '-'}}</div> 
      <formula-print :node="sub.term" :level="node.depth === 1 ? level : level + 1" :key="JSON.stringify(sub)+1"/> 
    </template>
  </template>
  <template v-else-if="node.op === 'var'">
    {{node.name}} 
  </template>
  <template v-else-if="node.op === 'const'">
    {{node.value}} 
  </template>
  <template v-else-if="node.op === 'div'">
    <formula-print :node="node.a" :level="node.depth === 1 ? level : level + 1"/>
    <div class="formula-op-div-line"></div> 
    <formula-print :node="node.b" :level="node.depth === 1 ? level : level + 1"/> 
  </template>
  <template v-else-if="node.op === 'if'">
    <div class="formula-op-if-ifthen">IF</div>
    <formula-print :node="node.p" :level="node.depth === 1 ? level : level + 1"/>
    <div class="formula-op-if-ifthen">THEN</div>
    <div class="formula-op-if-block">
    <formula-print :node="node.a" :level="node.depth === 1 ? level : level + 1"/>
    <div class="formula-op-if-else">ELSE</div>
    <formula-print :node="node.b" :level="node.depth === 1 ? level : level + 1"/>
    </div> 
  </template>
   <template v-else-if="node.pred_op === 'rel'">
    <formula-print :node="node.a" :level="node.depth === 1 ? level : level + 1"/>
    <div v-if="node.name === '='">=</div>
    <div v-else-if="node.name === '<'">&lt;</div>
    <div v-else-if="node.name === '<='">&le;</div>
    <div v-else-if="node.name === '!='">&ne;</div>
    <div v-else>{{node.name}}</div>
    <formula-print :node="node.b" :level="node.depth === 1 ? level : level + 1"/>
  </template>
  <template v-else-if="node.op === 'mul'">
    <template v-for="(factor, index) in node.factors">
      <div v-if="index > 0" :key="JSON.stringify(factor)+index">&middot;</div>
      <formula-print :node="factor" :level="node.depth === 1 ? level : level + 1" :key="JSON.stringify(factor)"/> 
    </template>
  </template> 
  <template v-else-if="node.op === 'pow'">
    <formula-print :node="node.a" :level="node.depth === 1 ? level : level + 1"/>
    <div>^</div> 
    <formula-print :node="node.b" :level="node.depth === 1 ? level : level + 1" class="formula-op-pow-exp"/> 
  </template>
  <template v-else-if="node.pred_op === 'and'">
    <template v-for="(pred, index) in node.preds">
      <div class="formula-op-and-column" :key="JSON.stringify(pred)+index">{{index == 0 ? '' : 'AND'}}</div>
      <formula-print :node="pred" :level="node.depth === 1 ? level : level + 1" :key="JSON.stringify(pred)"/> 
    </template>
  </template>
  <template v-else-if="node.op === 'func'">
    <div>{{node.name}}(</div> 
    <formula-print :node="node.param" :level="level"/> 
    <div>)</div>
  </template>
  <template v-else-if="node.pred_op === 'or'">
    <template v-for="(pred, index) in node.preds">
      <div v-if="index > 0" :key="JSON.stringify(pred)+index">OR</div>
      <formula-print :node="pred" :level="node.depth === 1 ? level : level + 1" :key="JSON.stringify(pred)"/> 
    </template>
  </template>
  <template v-else-if="node.pred_op === 'const'">
    {{node.value}}
  </template>    
  <template v-else>
    {{node}}
  </template>
</div>
</template>

<script>
export default {
  name: 'formula-print',
  props: [
    'node',
    'level'
  ],
  data () {
    return {
      maxLevel: 7,
    }
  },  
  computed: {
    levelClass() {
      return 'level' + (this.level <= this.maxLevel ? this.level : this.maxLevel);
    },
    formulaOpClass() {
      switch(this.node.op) {
        case 'add': return 'formula-op-add';
        case 'div': return 'formula-op-div';
        case 'if': return 'formula-op-if';
        case 'rel': return 'formula-op-rel';
        case 'mul': return 'formula-op-mul';
        case 'pow': return 'formula-op-pow';
        case 'func': return 'formula-op-func';
        default: 
          switch(this.node.pred_op) {
            case 'rel': return 'formula-op-rel';
            case 'and': return 'formula-op-and';
            case 'or': return 'formula-op-or';
            default: return '';
          }
      }
    }, 
  },
};
</script>

<style scoped>

.level0 {
  background-color: rgb(255, 255, 255);
  margin: 2px;
}

.level1 {
  background-color: rgb(240, 240, 240);
  margin: 2px;
}

.level2 {
  background-color: rgb(225, 225, 225);
  margin: 2px;
}

.level3 {
  background-color: rgb(210, 210, 210);
  margin: 2px;
}

.level4 {
  background-color: rgb(195, 195, 195);
  margin: 2px;
}

.level5 {
  background-color: rgb(180, 180, 180);
  margin: 2px;
}

.level6 {
  background-color: rgb(165, 165, 165);
  margin: 2px;
}

.level7 {
  background-color: rgb(150, 150, 150);
  margin: 2px;
}



.formula-op-add {
  display: grid; 
  grid-template-columns: max-content max-content; 
  align-items: center; 
  justify-items: right;
}

.formula-op-add-column {
  background-color: rgba(255, 255, 255, 0.411);
  justify-self: stretch;
  align-self: stretch;
  border-left: 1px solid #ffffff5e;
  display: flex;
  align-items: center;
  justify-content: center;
}

.formula-op-add-column-follow {
  margin-top: -3px;
  padding-bottom: 7px;
}

.formula-op-div {
  display: grid; 
  grid-template-columns: max-content;
  justify-items: center;
}

.formula-op-div-line {
  height: 2px;
  margin-left: 2px;
  margin-right: 2px;
  background-color: black;
  justify-self: stretch;
}

.formula-op-if {
  display: grid; 
  grid-template-columns: max-content max-content max-content max-content;
  justify-items: center;
  align-items: center;
}

.formula-op-if-ifthen {
  font-weight: bold;
}

.formula-op-if-block {
  border-left: 2px solid black;
  padding-left: 3px;
  margin-left: 1px;
  display: grid;
  grid-template-columns: max-content;
  justify-items: center;
  align-items: center;
}

.formula-op-if-else {
  justify-self: stretch;
  font-weight: bold;
  text-align: center;
  background-image: -webkit-linear-gradient(-141deg,transparent 0%,transparent 50%, white 50%, white 100%),
 -webkit-linear-gradient(  0deg, rgba(0,0,0,0.14) 0%, rgba(0,0,0,0.14) 50%, white 50%, white 100%);
 background-size:0.2em 0.2em;
}

.formula-op-rel {
  display: grid; 
  grid-template-columns: max-content max-content max-content;
  justify-items: center;
  align-items: center;
}

.formula-op-mul {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  align-items: center;
}

.formula-op-pow {
  display: grid; 
  grid-template-columns: max-content max-content max-content;
  justify-items: center;
  align-items: center;
}

.formula-op-pow-exp {
  margin-top: -2px;
  padding-top: 0px;
  padding-bottom: 10px;
}

.formula-op-and {
  display: grid; 
  grid-template-columns: max-content max-content; 
  align-items: center; 
  justify-items: right;
}

.formula-op-and-column {
  background-color: rgba(255, 255, 255, 0.411);
  justify-self: stretch;
  align-self: stretch;
  border-left: 1px solid #ffffff5e;
  display: flex;
  align-items: center;
  justify-content: center;
}

.formula-op-func {
  display: grid; 
  grid-template-columns: max-content max-content max-content;
  justify-items: center;
  align-items: center;
}

.formula-op-or {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  align-items: center;
}

</style>