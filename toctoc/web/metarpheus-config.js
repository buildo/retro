const path = require('path');
const cwd = process.cwd();
const modelPrelude = `// DO NOT EDIT MANUALLY - metarpheus-generated
/* eslint-disable */
import * as t from 'tcomb';
`;
const apiPrelude = `// DO NOT EDIT MANUALLY - metarpheus-generated
/* eslint-disable */
import * as t from 'tcomb';
import * as m from './model'
`;

module.exports = {
  modelPrelude,
  apiPrelude,
  apiPaths: [
    '../backend/src/main/scala'
  ].map(p => path.resolve(cwd, p)),
  overrides: {
    Instant: () => 't.Date',
    Unit: () => 't.interface({}, { strict: true, name: \'Unit\' })'
  },
  wiro: true,
  modelOut: path.resolve(cwd, 'src/metarpheus/model.js'),
  apiOut: path.resolve(cwd, 'src/metarpheus/api.js')
};
