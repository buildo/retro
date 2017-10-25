const path = require('path');
const cwd = process.cwd();
const modelPrelude = `/* tslint:disable */
`;

module.exports = {
  apiPaths: [
    '../backend/src/main/scala'
  ].map(p => path.resolve(__dirname, p)),
  modelOut: path.resolve(cwd, 'src/metarpheus/model-ts.ts'),
  apiOut: path.resolve(cwd, 'src/metarpheus/api-ts.ts'),
  modelPrelude,
  wiro: true
};
