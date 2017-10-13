import { SessionSerializer } from '../src/SessionSerializer'

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

