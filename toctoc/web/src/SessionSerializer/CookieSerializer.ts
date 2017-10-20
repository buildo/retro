import * as Cookies from 'cookies-js'
import SessionSerializer from './SessionSerializer'
import { TocTocToken } from '../metarpheus/model-ts'

export default <T = TocTocToken>() => SessionSerializer<T>({
  getter: Cookies.get,
  setter: Cookies.set
})
