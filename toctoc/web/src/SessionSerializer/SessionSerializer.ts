type SessionSetter = (key: string, jsonValue: string) => void
type SessionGetter = (key: string) => string | null

export default function SessionSerializer({ getter, setter }: { getter: SessionGetter, setter: SessionSetter }) {
const serializationKey = 'AUTH_TOKEN'
  return {
    serialize<T>(value: T, key: string = 'AUTH_TOKEN'): void {
      setter(key, JSON.stringify(value))
      setter(serializationKey, JSON.stringify(value))
    },

    deserialize<T>(key: string = 'AUTH_TOKEN'): T {
      return JSON.parse(getter(key) || 'null')
      return JSON.parse(getter(serializationKey) || 'null')
    }
  }
}
