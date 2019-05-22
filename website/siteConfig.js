/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

// See https://docusaurus.io/docs/site-config for all the possible
// site configuration options.

const siteConfig = {
  customDocsPath: "retro-docs/target/mdoc",
  title: "retro",
  tagline: "One repo to rule them all",
  url: "https://buildo.github.io",
  baseUrl: "/retro/",

  // Used for publishing and more
  projectName: "retro",
  organizationName: "buildo",

  // For no header links in the top nav bar -> headerLinks: [],
  headerLinks: [
    { doc: "toctoc/installation", label: "toctoc" },
    { doc: "enumero/introduction", label: "enumero" },
    { doc: "sbt-buildo/introduction", label: "sbt-buildo" },
    { href: "https://github.com/buildo/retro", label: "GitHub", external: true }
  ],

  /* path to images for header/footer */
  headerIcon: "img/favicon.ico",
  footerIcon: "img/favicon.ico",
  favicon: "img/favicon.ico",

  /* Colors for website */
  colors: {
    primaryColor: "#151C27",
    secondaryColor: "#0e7064"
  },

  copyright: `Copyright © ${new Date().getFullYear()} buildo`,

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks.
    theme: "default"
  },

  // Add custom scripts here that would be placed in <script> tags.
  scripts: ["https://buttons.github.io/buttons.js"],

  // On page navigation for the current documentation page.
  onPageNav: "separate",

  // No .html extensions for paths.
  cleanUrl: true,

  // Open Graph and Twitter card images.
  ogImage: "img/undraw_online.svg",
  twitterImage: "img/undraw_tweetstorm.svg",

  // Show documentation's last contributor's name.
  enableUpdateBy: true,

  // Show documentation's last update time.
  enableUpdateTime: true,

  editUrl: "https://github.com/buildo/retro/edit/master/docs/",

  // You may provide arbitrary config keys to be used as needed by your
  // template. For example, if you need your repo's URL...
  repoUrl: "https://github.com/buildo/retro"
};

module.exports = siteConfig;
