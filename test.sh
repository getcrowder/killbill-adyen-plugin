#
# Copyright 2020-2025 Equinix, Inc
# Copyright 2014-2025 The Billing Project, LLC
#
# The Billing Project licenses this file to you under the Apache License, version 2.0
# (the "License"); you may not use this file except in compliance with the
# License.  You may obtain a copy of the License at:
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.
#

curl -X POST \
  -H 'via: 1.1 heroku-router' \
  -H 'server: Heroku' \
  -H 'user-agent: Adyen HttpClient 1.0' \
  -H 'traceparent: 00-5c676a2aadc1579d6e287b2e9a35d4c4-c85586c7866c71b3-01' \
  -H 'content-type: application/json; charset=utf-8' \
  -H 'x-request-id: 0c39101a-33ca-c499-f92f-3e973d2d23a4' \
  -H 'authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'accept-encoding: gzip,deflate' \
  -H 'x-forwarded-for: 147.12.16.11' \
  -H 'x-request-start: 1747706762884' \
  -H 'x-forwarded-port: 443' \
  -H 'x-forwarded-proto: https' \
  'http://localhost/webhooks/adyen' \
  -d '{"live":"false","notificationItems":[{"NotificationRequestItem":{"amount":{"value":10000,"currency":"BRL"},"reason":"013843:9245:03/2030","success":"true","eventCode":"AUTHORISATION","eventDate":"2025-05-20T04:06:02+02:00","operations":["CANCEL","CAPTURE","REFUND"],"pspReference":"HB58458D29LJCK75","paymentMethod":"mc","additionalData":{"cardBin":"510322","authCode":"013843","expiryDate":"03/2030","cardSummary":"9245","fundingSource":"PREPAID","hmacSignature":"ti+QXMRuVOGfjiUjFlu8hYobS4u/Tld1AK23tRo45Bk=","issuerCountry":"BR","paymentMethod":"mc","cardHolderName":"Checkout Shopper PlaceHolder","shopperReference":"c08a5aa4-1d52-4491-a391-03e45a481a5e","checkout.cardAddedBrand":"mc","recurring.contractTypes":"ONECLICK,RECURRING","recurringProcessingModel":"UnscheduledCardOnFile","recurring.shopperReference":"c08a5aa4-1d52-4491-a391-03e45a481a5e","recurring.firstPspReference":"JJDN24ZB4M46KG75","tokenization.shopperReference":"c08a5aa4-1d52-4491-a391-03e45a481a5e","recurring.recurringDetailReference":"NZQM5QGRW44SXC75","tokenization.storedPaymentMethodId":"NZQM5QGRW44SXC75"},"merchantReference":"356e57af-1051-447a-95a8-290a92f8b6c5","merchantAccountCode":"TicketMaster_Brazil_TEST"}}]}'
