# Minusinė Bramos akis

![](https://github.com/dainiusjocas/minusine-bramos-akis/workflows/Linting%20and%20Unit%20Tests/badge.svg)

<img src="doc/goldman-sachs.jpg"
 alt="Goldman Sachs but not Like Hillary" title="Goldman Sachs but not Like Hillary"
 align="right" />

> "'Minusinė Bramos akis' - pusiau sąmoningas randominis neuronų tinklas, suteikiantis ribotą prieigą prie visų praeities įvykių, palikusių elektroninį arba šviesos atspaudą, net jeigu tas atspaudas jau sunaikintas."
> - Viktor Pelevin, "iPhuck 10", 2017, 207-208pp"

## Lambda Usage

Set environment variables:
- API_ID, as documented [here](https://docs.aws.amazon.com/lambda/latest/dg/with-on-demand-https-example.html)
- AWS_DEFAULT_REGION

```bash
curl -s -X POST https://$API_ID.execute-api.$AWS_DEFAULT_REGION.amazonaws.com/Prod/observe -d '
{
  "dictionary": [{"text":"Jocas"}],
  "search": {
    "url": "jocas.lt"
 }
}
' | json_pp
```

This request should yield:
```json
[
   {
      "hits" : [
         {
            "dict-entry-id" : "0",
            "text" : "Karbauskis",
            "meta" : {},
            "end-offset" : 4408,
            "type" : "PHRASE",
            "begin-offset" : 4398
         }
      ],
      "original" : "https://www.delfi.lt/news/daily/crime/alytaus-policija-iesko-nuo-bausmes-besislapstancio-vyro.d?id=77416031",
      "archive-url" : "http://web.archive.org/web/20180314121007/https://www.delfi.lt/news/daily/crime/alytaus-policija-iesko-nuo-bausmes-besislapstancio-vyro.d?id=77416031"
   },
   {
      "hits" : [
         {
            "meta" : {},
            "end-offset" : 3777,
            "type" : "PHRASE",
            "begin-offset" : 3767,
            "text" : "Karbauskis",
            "dict-entry-id" : "0"
         }
      ],
      "original" : "https://www.delfi.lt/news/daily/crime/atsauke-pavoju-sostines-prekybos-centre.d?id=77264831",
      "archive-url" : "http://web.archive.org/web/20180314032345/https://www.delfi.lt/news/daily/crime/atsauke-pavoju-sostines-prekybos-centre.d?id=77264831"
   }
]
```

## License

Copyright &copy; 2019 [Dainius Jocas](https://www.jocas.lt).

Distributed under the The Apache License, Version 2.0.
