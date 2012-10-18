/**
 * node test.js <idx>
 * @param idx 运行哪个测试程序，0运行恢复程序，1或其余值运行业务流程
 */
var test_biz_flow = require('./test_biz_flow');
var test_recover = require('./test_recover');

if(process.argv.length < 2) {
  return console.log('Usage: node test.js 0|1');
}

if(process.argv[2] == '0') {
  test_recover.main();
}
else {
  test_biz_flow.main();
}
  
