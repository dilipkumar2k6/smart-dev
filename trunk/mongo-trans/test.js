var test_biz_flow = require('./test_biz_flow');
var test_recover = require('./test_recover');



if(process.argv[2] == '0') {
  test_recover.main();

}
else {
  test_biz_flow.main();
}
  
