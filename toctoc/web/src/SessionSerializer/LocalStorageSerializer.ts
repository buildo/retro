import SessionSerializer from './SessionSerializer'

export default SessionSerializer({
  getter: window.localStorage.getItem,
  setter: window.localStorage.setItem
})
