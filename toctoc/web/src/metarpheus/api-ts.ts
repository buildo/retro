

// DO NOT EDIT MANUALLY - metarpheus-generated
import axios, { AxiosError } from 'axios'
import * as t from 'io-ts'
import * as m from './model-ts'

export interface RouteConfig {
  apiEndpoint: string,
  timeout: number,
  unwrapApiResponse: (resp: any) => any
}

import { PathReporter } from 'io-ts/lib/PathReporter'
function valueOrThrow<T extends t.Type<any, any>>(iotsType: T, value: T['_I']): t.TypeOf<T> {
  const validatedValue = iotsType.decode(value);

  if (validatedValue.isLeft()) {
    throw new Error(PathReporter.report(validatedValue).join('\n'));
  }

  return validatedValue.value;
}

const parseError = (err: AxiosError) => {
  try {
    const { errors = [] } = err.response!.data;
    return Promise.reject({ status: err.response!.status, errors });
  } catch (e) {
    return Promise.reject({ status: err && err.response && err.response.status || 0, errors: [] });
  }
};

export default function getRoutes(config: RouteConfig) {
  return {
    tokenAuthenticationController_login: function ({ login }: { login: m.Login }): Promise<m.TocTocToken> {
      return axios({
        method: 'post',
        url: `${config.apiEndpoint}/toctoc/login`,
        params: {

        },
        data: {
          login
        },
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: config.timeout
      }).then(res => valueOrThrow(m.TocTocToken, config.unwrapApiResponse(res.data)), parseError) as any
    },

    tokenAuthenticationController_refresh: function ({ refreshToken }: { refreshToken: m.RefreshToken }): Promise<m.TocTocToken> {
      return axios({
        method: 'post',
        url: `${config.apiEndpoint}/toctoc/refresh`,
        params: {

        },
        data: {
          refreshToken
        },
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: config.timeout
      }).then(res => valueOrThrow(m.TocTocToken, config.unwrapApiResponse(res.data)), parseError) as any
    },

    tokenAuthenticationController_logout: function ({ token }: { token: string }): Promise<m.Unit> {
      return axios({
        method: 'post',
        url: `${config.apiEndpoint}/toctoc/logout`,
        params: {

        },
        data: {

        },
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Token token="${token}"`
        },
        timeout: config.timeout
      }).then(res => valueOrThrow(m.Unit, config.unwrapApiResponse(res.data)), parseError) as any
    }
  }
}
