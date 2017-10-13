import * as Cookies from 'cookies-js'
import SessionSerializer from './SessionSerializer';

export default SessionSerializer({
  getter: Cookies.get,
  setter: Cookies.set
})
