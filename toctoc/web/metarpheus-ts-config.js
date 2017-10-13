const path = require('path');
const cwd = process.cwd();

module.exports = {
  apiPaths: [
    '../backend/src/main/scala'
  ].map(p => path.resolve(__dirname, p)),
  modelOut: path.resolve(cwd, 'src/metarpheus/model-ts.ts'),
  apiOut: path.resolve(cwd, 'src/metarpheus/api-ts.ts'),
  modelPrelude: `/* tslint:disable */
`,
  wiro: true
};
