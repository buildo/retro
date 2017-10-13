type SessionSetter = (key: string, jsonValue: string) => void
type SessionGetter = (key: string) => string

export default function SessionSerializer({ getter, setter }: { getter: SessionGetter, setter: SessionSetter }) {
  return {
    serialize<T>(value: T, key: string = 'AUTH_TOKEN'): void {
      setter(key, JSON.stringify(value))
    },

    deserialize<T>(key: string = 'AUTH_TOKEN'): T {
      return JSON.parse(getter(key) || 'null')
    }
  }
}
