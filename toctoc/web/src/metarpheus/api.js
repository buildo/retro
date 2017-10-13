
// DO NOT EDIT MANUALLY - metarpheus-generated
/* eslint-disable */
import t from 'tcomb';

import * as m from './model';


export default [
  // POST /toctoc/login : 
  {
    method: 'post',
    name: ['tokenAuthenticationController', 'login'],
    authenticated: false,
    returnType: m.TocTocToken,
    route: (...routeParams) => ['toctoc', 'login'].join('/'),
    routeParamTypes: [],
    params: {
      
    },
    body: t.interface({
      login: m.Login
    })
  },

  // POST /toctoc/logout : 
  {
    method: 'post',
    name: ['tokenAuthenticationController', 'logout'],
    authenticated: true,
    returnType: t.Nil,
    route: (...routeParams) => ['toctoc', 'logout'].join('/'),
    routeParamTypes: [],
    params: {
      
    }
  },

  // POST /toctoc/refresh : 
  {
    method: 'post',
    name: ['tokenAuthenticationController', 'refresh'],
    authenticated: false,
    returnType: m.TocTocToken,
    route: (...routeParams) => ['toctoc', 'refresh'].join('/'),
    routeParamTypes: [],
    params: {
      
    },
    body: t.interface({
      refreshToken: m.RefreshToken
    })
  }
];
