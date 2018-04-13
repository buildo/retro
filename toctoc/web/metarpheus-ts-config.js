const path = require('path');
const cwd = process.cwd();

const modelPrelude = `export interface Unit {};
export const Unit = t.strict({}, 'Unit');

`;

module.exports = {
  modelPrelude,
  apiPaths: [
    '../backend/wiro/src/main/scala',
    '../backend/core/src/main/scala'
  ].map(p => path.resolve(__dirname, p)),
  modelOut: path.resolve(cwd, 'src/metarpheus/model-ts.ts'),
  apiOut: path.resolve(cwd, 'src/metarpheus/api-ts.ts'),
  wiro: true
};
