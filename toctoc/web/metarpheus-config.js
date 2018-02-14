const path = require('path');
const cwd = process.cwd();

module.exports = {
  apiPaths: [
    '../backend/wiro/src/main/scala',
    '../backend/core/src/main/scala'
  ].map(p => path.resolve(cwd, p)),
  overrides: {
    Instant: () => 't.Date',
    Unit: () => 't.interface({}, { strict: true, name: \'Unit\' })'
  },
  wiro: true,
  modelOut: path.resolve(cwd, 'src/metarpheus/model.js'),
  apiOut: path.resolve(cwd, 'src/metarpheus/api.js')
};
