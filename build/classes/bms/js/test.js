var java = require("java");
var ArrayList = java.import('java.util.ArrayList');
var list = new ArrayList();
list.addSync('item1');
console.log("hello "+list.getSync(0));