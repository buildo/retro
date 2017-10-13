import { SessionSerializer, CookieSerializer } from '../src/SessionSerializer'
import * as Cookies from 'cookies-js'

describe('SessionSerializer', () => {
  const value = {
    foo: 'bar'
  }
  const Serializer = SessionSerializer({
    setter: (key, value) => expect(value).toBe('{"foo":"bar"}'),
    getter: (key) => JSON.stringify(value)
  })

  it('should stringify a serialized value', () => {
    Serializer.serialize(value)
  })

  it('should return a deserialized value', () => {
    Serializer.deserialize('AUTH_TOKEN')
  })
})

describe('CookieSerializer', () => {

  const value = {
    foo: 42
  }

  it('should serialize a value in cookies', () => {
    CookieSerializer.serialize(value)
    expect(Cookies.get('AUTH_TOKEN')).toBe(JSON.stringify(value))
  })

  it('should deserialize a value from cookies', () => {
    CookieSerializer.serialize(value)
    expect(CookieSerializer.deserialize('AUTH_TOKEN')).toEqual(value)
  })
})
