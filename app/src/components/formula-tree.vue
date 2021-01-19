<template>
  <!--<div :class="levelClass" style="display: inline-block; text-align: center; vertical-align: middle;">-->
      <div class="row no-wrap justify-center items-center" :class="levelClass" >
        <template v-if="isValue">
          {{node}}
        </template>
        <template v-else-if="key === '+'">
          <div class="formula-add">
            <div class="formula-add-column"></div> <formula-tree :node="node[key][0]" :level="node1Level"/> 
            <div class="formula-add-column">+</div> <formula-tree :node="node[key][1]" :level="node2Level"/>
          </div>
        </template>
        <template v-else-if="key === '-'">
          <div class="formula-add">
            <div class="formula-add-column"></div> <formula-tree :node="node[key][0]" :level="node1Level"/> 
            <div class="formula-add-column">-</div> <formula-tree :node="node[key][1]" :level="node2Level"/>
          </div>
        </template>
        <template v-else-if="key === '*'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> &middot; <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '/'">
          <div class="column no-wrap justify-center items-center"><formula-tree :node="node[key][0]" :level="level"/> <div class="self-stretch formula-div-line"></div> <formula-tree :node="node[key][1]" :level="level"/></div>
        </template>
        <template v-else-if="key === '^'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> ^ <formula-tree :node="node[key][1]" :level="node2Level" class="fomula-exponent"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '?'">
          <span v-if="parentheses">(</span>
          <b>IF</b> <formula-tree :node="node[key][0]" :level="node1Level"/> <b>THEN</b> <div class="column no-wrap justify-center items-center formula-if-alternative-block"><formula-tree :node="node[key][1]" :level="node2Level"/><div class="self-stretch formula-if-else"><b>ELSE</b></div><formula-tree :node="node[key][2]" :level="node3Level"/></div>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '&&'">
          <div class="formula-add">
            <div class="formula-add-column"></div> <formula-tree :node="node[key][0]" :level="node1Level"/> 
            <div class="formula-add-column">AND</div> <formula-tree :node="node[key][1]" :level="node2Level"/>
          </div>
        </template>
        <template v-else-if="key === '||'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> OR <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>        
        <template v-else-if="key === '=='">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> = <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '<'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> &lt; <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '<='">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> &le; <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>             
        <template v-else-if="key === '>'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> &gt; <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '>='">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> &ge; <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '!='">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> &ne; <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="node[key].length === 1">
          {{key}}
          <span>(</span>
          <formula-tree :node="node[key][0]" :level="level"/>
          <span>)</span>
        </template>        
        <template v-else>
          {{node}}
        </template>
  </div>
</template>

<script>
export default {
  name: 'formula-tree',
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
    isValue() {
      return typeof this.node !== 'object' || this.node === null;
    },
    key() {
      if(this.isValue) {
        return undefined;
      }
      return Object.keys(this.node)[0];
    },
    levelClass() {
      return 'level' + (this.level <= this.maxLevel ? this.level : this.maxLevel);
    },
    parentheses() {
      return this.level > this.maxLevel;
    },
    node1() {
      if(this.isValue || this.node[this.key].length < 1) {
        return undefined;
      }
      return this.node[this.key][0];
    },
    node2() {
      if(this.isValue || this.node[this.key].length < 2) {
        return undefined;
      }
      return this.node[this.key][1];
    },
    node3() {
      if(this.isValue || this.node[this.key].length < 2) {
        return undefined;
      }
      return this.node[this.key][1];
    },
    node1Level() {
      return typeof this.node1 !== 'object' || this.node1 === null ? this.level : this.level + 1;
    },
    node2Level() {
      return typeof this.node2 !== 'object' || this.node2 === null ? this.level : this.level + 1;
    },
    node3Level() {
      return typeof this.node3 !== 'object' || this.node3 === null ? this.level : this.level + 1;
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

.formula-div-line {
  height: 2px;
  margin-left: 2px;
  margin-right: 2px;
  background-color: black;
}

.formula-if-alternative-block {
  border-left: 2px solid black;
  padding-left: 3px;
  margin-left: 1px;
}

.formula-if-else {
  text-align: center;

  background-image: -webkit-linear-gradient(-141deg,transparent 0%,transparent 50%, white 50%, white 100%),
 -webkit-linear-gradient(  0deg, rgba(0,0,0,0.14) 0%, rgba(0,0,0,0.14) 50%, white 50%, white 100%);
 background-size:0.2em 0.2em;
}

.fomula-exponent {
  padding-bottom: 10px;
}

.formula-add {
  display: grid; 
  grid-template-columns: max-content max-content; 
  align-items: center; 
  justify-items: left;
}

.formula-add-column {
  background-color: rgba(255, 255, 255, 0.411);
  justify-self: stretch;
  align-self: stretch;
  border-left: 1px solid #ffffff5e;
  display: flex;
  align-items: center;
  justify-content: center;
}

</style>