const path = require('path')
const fs   = require('fs')

const data_file = path.join(__dirname, '02h1. example - API result - get list of supported languages.txt')
const data_json = JSON.parse( fs.readFileSync(data_file, {encoding: 'utf8'}) )

data_json.sort((a,b) => (a.english_name < b.english_name) ? -1 : ((a.english_name > b.english_name) ? 1 : 0))

const languages = data_json.map(a => a.english_name)
const langcodes = data_json.map(a => a.iso_639_1)

const indent = '  '

const donottranslate = `
<string-array name="languages" translatable="false">
${indent}<item>${languages.join(`</item>\n${indent}<item>`)}</item>
</string-array>
<string-array name="langcodes" translatable="false">
${indent}<item>${langcodes.join(`</item>\n${indent}<item>`)}</item>
</string-array>
`

console.log(donottranslate.trim())
