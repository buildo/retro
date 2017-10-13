const path = require('path');
const cwd = process.cwd();
const modelPrelude = `// DO NOT EDIT MANUALLY - metarpheus-generated
/* eslint-disable */
import t from 'tcomb';
`;

module.exports = {
  modelPrelude,
  apiPaths: [
    '../backend/src/main/scala'
  ].map(p => path.resolve(cwd, p)),
  wiro: true,
  modelOut: path.resolve(cwd, 'src/metarpheus/model.js'),
  apiOut: path.resolve(cwd, 'src/metarpheus/api.js')
};
