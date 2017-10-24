import SessionSerializer from './SessionSerializer'
import { TocTocToken } from '../metarpheus/model-ts'

export default <T = TocTocToken>() => SessionSerializer<T>({
  getter: window.localStorage.getItem,
  setter: window.localStorage.setItem
})
