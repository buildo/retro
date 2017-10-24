import { TocTocToken } from '../metarpheus/model-ts'

type SessionSetter = (key: string, stringifiedValue: string) => void
type SessionGetter = (key: string) => string | null

const serializationKey = 'AUTH_TOKEN'

export default function SessionSerializer<T = TocTocToken>({ getter, setter }: { getter: SessionGetter, setter: SessionSetter }) {
  return {
    serialize(value: T): void {
      setter(serializationKey, JSON.stringify(value))
    },

    deserialize(): T {
      return JSON.parse(getter(serializationKey) || 'null')
    }
  }
}
