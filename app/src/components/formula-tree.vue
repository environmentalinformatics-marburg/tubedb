<template>
  <div :class="levelClass" style="display: inline-block; text-align: center; vertical-align: middle;">
        <template v-if="isValue">
          {{node}}
        </template>
         <template v-else-if="key === '+'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> + <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '-'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> - <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '*'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> &times; <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '/'">
        <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="level"/> <hr> <formula-tree :node="node[key][1]" :level="level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '^'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> ^ <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '?'">
          <span v-if="parentheses">(</span>
          <div style="display: inline-block; text-align: center; vertical-align: middle;">IF <formula-tree :node="node[key][0]" :level="node1Level"/> THEN</div> <div style="display: inline-block;"><formula-tree :node="node[key][1]" :level="node2Level"/> <br>ELSE<br> <formula-tree :node="node[key][2]" :level="node3Level"/></div>
          <span v-if="parentheses">)</span>
        </template>
        <template v-else-if="key === '&&'">
          <span v-if="parentheses">(</span>
          <formula-tree :node="node[key][0]" :level="node1Level"/> AND <formula-tree :node="node[key][1]" :level="node2Level"/>
          <span v-if="parentheses">)</span>
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

</style>