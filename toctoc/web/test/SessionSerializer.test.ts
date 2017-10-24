import { SessionSerializer, CookieSerializer, LocalStorageSerializer } from '../src/SessionSerializer'
import * as Cookies from 'cookies-js'

type FooType = {
  foo: number
}

describe('SessionSerializer', () => {
  const value = {
    foo: 42
  }
  const Serializer = SessionSerializer<FooType>({
    setter: (key, value) => expect(value).toBe('{"foo":42}'),
    getter: (key) => JSON.stringify(value)
  })

  it('should stringify a serialized value', () => {
    Serializer.serialize(value)
  })

  it('should return a deserialized value', () => {
    expect(Serializer.deserialize()).toEqual(value)
  })
})

describe('CookieSerializer', () => {
  const value = {
    foo: 42
  }
  const Serializer = CookieSerializer<FooType>()

  it('should serialize a value in cookies', () => {
    Serializer.serialize(value)
    expect(Cookies.get('AUTH_TOKEN')).toBe(JSON.stringify(value))
  })

  it('should deserialize a value from cookies', () => {
    Serializer.serialize(value)
    expect(Serializer.deserialize()).toEqual(value)
  })
})

describe('LocalStorageSerializer', () => {
  const value = {
    foo: 42
  }
  const Serializer = LocalStorageSerializer<FooType>()

  it('should serialize a value in localStorage', () => {
    Serializer.serialize(value)
    expect(localStorage.getItem('AUTH_TOKEN')).toBe(JSON.stringify(value))
  })

  it('should deserialize a value from localStorage', () => {
    Serializer.serialize(value)
    expect(Serializer.deserialize()).toEqual(value)
  })
})
