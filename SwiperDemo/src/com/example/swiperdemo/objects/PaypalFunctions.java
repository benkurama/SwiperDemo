package com.example.swiperdemo.objects;

import java.net.URLDecoder.*;
import java.util.*;
import java.util.HashMap;
import java.util.StringTokenizer.*;
import java.io.*;
import java.net.*;
//import javax.servlet.http.HttpServletResponse;

public class PaypalFunctions
{

    private String gv_APIUser;
    private String gv_APIPassword;
    private String gv_APIVendor;
    private String gv_APIPartner;
    private String gv_APIEndpoint;
    private String gv_BNCode;
    private String gv_Env;
    private String gv_nvpHeader;
    private String gv_ProxyServer;
    private String gv_ProxyServerPort;
    private String unique_id;
    private int gv_Proxy;
    private boolean gv_UseProxy;
    private String PAYPAL_URL;

    public PaypalFunctions()
    {//lhuynh - Actions to be Done on init of this class

        //BN Code is only applicable for partners
        gv_BNCode = "PF-CCWizard";
        gv_APIUser = "alvinsison";
        //Fill in the gv_APIPassword variable yourself, the wizard will not do this automatically 
        gv_APIPassword = "aoitenshi001";
        gv_APIVendor = "benkurama";
        gv_APIPartner = "PayPal";
        gv_Env = "pilot";
        unique_id = "";

        boolean bSandbox = true;

        /*
        Servers for NVP API
        Sandbox: https://pilot-payflowpro.paypal.com
        Live: https://payflowpro.paypal.com
         */

        /*
        Redirect URLs for PayPal Login Screen
        Sandbox: https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=XXXX
        Live: https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=XXXX
         */

        if ("pilot".equals(gv_Env))
        {
            gv_APIEndpoint = "https://pilot-payflowpro.paypal.com";
            PAYPAL_URL = "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=";
        }
        else
        {
            gv_APIEndpoint = "https://payflowpro.paypal.com";
            PAYPAL_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=";
        }

        String HTTPREQUEST_PROXYSETTING_SERVER = "";
        String HTTPREQUEST_PROXYSETTING_PORT = "";
        boolean USE_PROXY = false;

        //WinObjHttp Request proxy settings.
        gv_ProxyServer = HTTPREQUEST_PROXYSETTING_SERVER;
        gv_ProxyServerPort = HTTPREQUEST_PROXYSETTING_PORT;
        gv_Proxy = 2;	//'setting for proxy activation
        gv_UseProxy = USE_PROXY;


    }

    /*********************************************************************************
     * CallShortcutExpressCheckout: Function to perform the SetExpressCheckout API call
     *
     * Inputs:
     *		paymentAmount:  	Total value of the shopping cart
     *		currencyCodeType: 	Currency code value the PayPal API
     *		paymentType: 		paymentType has to be one of the following values: Sale or Order
     *		returnURL:			the page where buyers return to after they are done with the payment review on PayPal
     *		cancelURL:			the page where buyers return to when they cancel the payment review on PayPal
     *
     * Output: Returns a HashMap object containing the response from the server.
     *********************************************************************************/
    public HashMap CallShortcutExpressCheckout(String paymentAmount, String returnURL, String cancelURL)
    {

        /*
        '------------------------------------
        ' The currencyCodeType and paymentType 
        ' are set to the selections made on the Integration Assistant 
        '------------------------------------
         */

        String currencyCodeType = "<CURRENCY_CODE>";
        String paymentType = "<PAYMENT_TYPE>";

        /*
        Construct the parameter string that describes the PayPal payment
        the varialbes were set in the web form, and the resulting string
        is stored in $nvpstr
         */
        String nvpstr = "&TENDER=P&ACTION=S";
        if ("Authorization".equals(paymentType))
        {
            nvpstr = nvpstr + "&TRXTYPE=A";
        }
        else /* sale */

        {
            nvpstr = nvpstr + "&TRXTYPE=S";
        }

        nvpstr = nvpstr + "&AMT=" + paymentAmount + "&RETURNURL=" + returnURL + "&CANCELURL=" + cancelURL + "&CURRENCY=" + currencyCodeType;

        /*
        Make the call to Payflow to get the Express Checkout token
        If the API call succeded, then redirect the buyer to PayPal
        to begin to authorize payment.  If an error occured, show the
        resulting errors
         */

        /* requires at least Java 5 */
        UUID uid = UUID.randomUUID();

        HashMap nvp = httpcall(nvpstr, uid.toString());
        String strAck = nvp.get("RESULT").toString();
        if (strAck != null && strAck.equalsIgnoreCase("0"))
        {
            return nvp;
        }

        return null;
    }

    /*********************************************************************************
     * CallMarkExpressCheckout: Function to perform the SetExpressCheckout API call
     *
     * Inputs:
     *		paymentAmount:  	Total value of the shopping cart
     *		currencyCodeType: 	Currency code value the PayPal API
     *		paymentType: 		paymentType has to be one of the following values: Sale or Order or Authorization
     *		returnURL:			the page where buyers return to after they are done with the payment review on PayPal
     *		cancelURL:			the page where buyers return to when they cancel the payment review on PayPal
     *		shipToName:		the Ship to name entered on the merchant's site
     *		shipToStreet:		the Ship to Street entered on the merchant's site
     *		shipToCity:			the Ship to City entered on the merchant's site
     *		shipToState:		the Ship to State entered on the merchant's site
     *		shipToCountryCode:	the Code for Ship to Country entered on the merchant's site
     *		shipToZip:			the Ship to ZipCode entered on the merchant's site
     *		shipToStreet2:		the Ship to Street2 entered on the merchant's site
     *		phoneNum:			the phoneNum  entered on the merchant's site
     *
     * Output: Returns a HashMap object containing the response from the server.
     *********************************************************************************/
    public HashMap CallMarkExpressCheckout(String paymentAmount, String returnURL, String cancelURL, String shipToName, String shipToStreet, String shipToCity, String shipToState,
                                           String shipToCountryCode, String shipToZip, String shipToStreet2, String phoneNum)
    {
        /*
        '------------------------------------
        ' The currencyCodeType and paymentType 
        ' are set to the selections made on the Integration Assistant 
        '------------------------------------
         */
        String currencyCodeType = "<CURRENCY_CODE>";
        String paymentType = "<PAYMENT_TYPE>";

        /*
        Construct the parameter string that describes the PayPal payment
        the variables were set in the web form, and the resulting string
        is stored in $nvpstr
         */
        String nvpstr = "&TENDER=P&ACTION=S";
        if ("Authorization".equals(paymentType))
        {
            nvpstr = nvpstr + "&TRXTYPE=A";
        }
        else /* sale */

        {
            nvpstr = nvpstr + "&TRXTYPE=S";
        }

        nvpstr = nvpstr + "ADDROVERRIDE=1&AMT=" + paymentAmount;
        nvpstr = nvpstr.concat("&CURRENCYCODE=" + currencyCodeType + "&RETURNURL=" + returnURL + "&CANCELURL=" + cancelURL);

        nvpstr = nvpstr.concat("&SHIPTOSTREET=" + shipToStreet + "&SHIPTOSTREET2=" + shipToStreet2);
        nvpstr = nvpstr.concat("&SHIPTOCITY=" + shipToCity + "&SHIPTOSTATE=" + shipToState + "&SHIPTOCOUNTRY=" + shipToCountryCode);
        nvpstr = nvpstr.concat("&SHIPTOZIP=" + shipToZip);

        /*
        Make the call to PayPal to set the Express Checkout token
        If the API call succeded, then redirect the buyer to PayPal
        to begin to authorize payment.  If an error occured, show the
        resulting errors
         */

        /* requires at least Java 5 */
        UUID uid = UUID.randomUUID();

        HashMap nvp = httpcall(nvpstr, uid.toString());
        String strAck = nvp.get("RESULT").toString();
        if (strAck != null && strAck.equalsIgnoreCase("0"))
        {
            return nvp;
        }

        return null;
    }

    /*********************************************************************************
     * GetShippingDetails: Function to perform the GetExpressCheckoutDetails API call
     *
     * Inputs:  None
     *
     * Output: Returns a HashMap object containing the response from the server.
     *********************************************************************************/
    public HashMap GetShippingDetails(String token)
    {
        /*
        Build a second API request to PayPal, using the token as the
        ID to get the details on the payment authorization
         */

        String nvpstr = "&TOKEN=" + token + "&TENDER=P&ACTION=G";
        if ("Authorization" == "<PAYMENT_TYPE>")
        {
            nvpstr = nvpstr + "&TRXTYPE=A";
        }
        else /* sale */

        {
            nvpstr = nvpstr + "&TRXTYPE=S";
        }

        /*
        Make the API call and store the results in an array.  If the
        call was a success, show the authorization details, and provide
        an action to complete the payment.  If failed, show the error
         */

        /* requires at least Java 5 */
        UUID uid = UUID.randomUUID();

        HashMap nvp = httpcall(nvpstr, uid.toString());
        String strAck = nvp.get("RESULT").toString();
        if (strAck != null && strAck.equalsIgnoreCase("0"))
        {
            return nvp;
        }
        return null;
    }

    /*********************************************************************************
     * GetShippingDetails: Function to perform the DoExpressCheckoutPayment API call
     *
     * Inputs:  None
     *
     * Output: Returns a HashMap object containing the response from the server.
     *********************************************************************************/
    public HashMap ConfirmPayment(String token, String payerID, String finalPaymentAmount, String serverName)
    {

        /*
        '------------------------------------
        ' The currencyCodeType and paymentType 
        ' are set to the selections made on the Integration Assistant 
        '------------------------------------
         */
        String currencyCodeType = "<CURRENCY_CODE>";
        String paymentType = "<PAYMENT_TYPE>";

        /*
        '----------------------------------------------------------------------------
        '----	Use the values stored in the session from the previous SetEC call
        '----------------------------------------------------------------------------
         */
        String nvpstr = "&TOKEN=" + token + "&TENDER=P&ACTION=D";
        if ("Authorization" == paymentType)
        {
            nvpstr = nvpstr + "&TRXTYPE=A";
        }
        else /* sale */

        {
            nvpstr = nvpstr + "&TRXTYPE=S";
        }

        nvpstr = nvpstr + "&PAYERID=" + payerID + "&AMT=" + finalPaymentAmount;

        nvpstr = nvpstr + "&CURRENCY=" + currencyCodeType + "&IPADDRESS=" + serverName;

        /*
        Make the call to PayPal to finalize payment
        If an error occured, show the resulting errors
         */
        //String sessionuuid = session.getAttribute("unique_id");

        if ("" == unique_id)
        {
            /* requires at least Java 5 */
            UUID uid = UUID.randomUUID();
            unique_id = uid.toString();
        }

        HashMap nvp = httpcall(nvpstr, unique_id);
        String strAck = nvp.get("RESULT").toString();
        if (strAck != null && strAck.equalsIgnoreCase("0"))
        {
            return nvp;
        }
        return null;

    }

    /*********************************************************************************
     * DirectPayment: Function to perform credit card transactions
     *
     * Inputs:
     *		paymentType: 		paymentType has to be one of the following values: Sale or Order
     *		paymentAmount:  	Total value of the shopping cart
     *		creditCardType		Credit card type has to one of the following values: Visa or MasterCard or Discover or Amex or Switch or Solo 
     *		creditCardNumber	Credit card number
     *		expDate				Credit expiration date
     *		cvv2				CVV2
     *		firstName			Customer's First Name
     *		lastName			Customer's Last Name
     *		street				Customer's Street Address
     *		city				Customer's City
     *		state				Customer's State				
     *		zip					Customer's Zip					
     *		countryCode			Customer's Country represented as a PayPal CountryCode
     *		currencyCode		Customer's Currency represented as a PayPal CurrencyCode
     *		orderdescription	Short textual description of the order
     *
     * Output: Returns a HashMap object containing the response from the server.
     *********************************************************************************/
    public HashMap DirectPayment(String paymentType, String paymentAmount, String creditCardType, String creditCardNumber, String expDate, String cvv2, String firstName, String lastName, String street, String city, String state, String zip, String countryCode, String currencyCode, String orderdescription)
    {
        /*
        '------------------------------------
        ' The currencyCodeType and paymentType 
        ' are set to the selections made on the Integration Assistant 
        '------------------------------------
         */

        /*
        Construct the parameter string that describes the PayPal payment
        the variables were set in the web form, and the resulting string
        is stored in $nvpstr
         */
        String nvpstr = "&TENDER=C";
        if ("Authorization".equals(paymentType))
        {
            nvpstr = nvpstr + "&TRXTYPE=A";
        }
        else /* sale */

        {
            nvpstr = nvpstr + "&TRXTYPE=S";
        }

        /* requires at least Java 5 */
        UUID uid = UUID.randomUUID();

        nvpstr = nvpstr + "&ACCT=" + creditCardNumber + "CVV2=" + cvv2 + "&EXPDATE=" + expDate + "&ACCTTYPE=" + creditCardType;
        nvpstr = nvpstr + "&AMT=" + paymentAmount + "&CURRENCY=" + currencyCode;
        nvpstr = nvpstr + "&FIRSTNAME=" + firstName + "&LASTNAME=" + lastName + "&STREET=" + street + "&CITY=" + city;
        nvpstr = nvpstr + "&STATE=" + state + "&ZIP=" + zip + "&COUNTRY=" + countryCode;
        nvpstr = nvpstr + "&INVNUM=" + uid.toString() + "&ORDERDESC=" + orderdescription;

        /*
        Make the call to PayPal to set the Express Checkout token
        If the API call succeded, then redirect the buyer to PayPal
        to begin to authorize payment.  If an error occured, show the
        resulting errors
         */

        HashMap nvp = httpcall(nvpstr, uid.toString());
        String strAck = nvp.get("RESULT").toString();
        if (strAck != null && strAck.equalsIgnoreCase("0"))
        {
            return nvp;
        }

        return null;
    }

    /*********************************************************************************
     * httpcall: Function to perform the API call to PayPal using API signature
     * 	@ methodName is name of API  method.
     * 	@ nvpStr is nvp string.
     * returns a NVP string containing the response from the server.
     *********************************************************************************/
    public HashMap httpcall(String nvpStr, String unique_id)
    {
        String agent = "";
        String respText = "";
        HashMap nvp = null;   //lhuynh not used?

        //nvp = deformatNVP( nvpStr );
        //String encodedData = "&PWD=" + gv_APIPassword + "&USER=" + gv_APIUser + "&VENDOR=" + gv_APIVendor + "&PARTNER=" + gv_APIPartner + nvpStr + "&BUTTONSOURCE=" + gv_BNCode;
        String encodedData = "?USER=benkurama"
                + "&PWD=Redfoot123_"
                + "&VERSION=58.0"
                + "&PAYMENTACTION=Authorization"
                + "&CREDITCARDTYPE=Visa"
                + "&ACCT=4405435173537581"
                + "&STARTDATE=92016"
                + "&EXPDATE=92016&CVV2=123"
                + "&AMT=20.00"
                + "&CURRENCYCODE=USD"
                + "&FIRSTNAME=Aung"
                + "&LASTNAME=Thaw+Aye"
                + "&STREET="
                + "&STREET2="
                + "&CITY=San+Francisco"
                + "&STATE=CA"
                + "&Zip=94121"
                + "&COUNTRYCODE=US"
                + "&EMAIL=alvin.sison@redfoottech.com"
                + "&METHOD=DoDirectPayment";
        try
        {
            URL postURL = new URL("https://api.sandbox.paypal.com/nvp");
            HttpURLConnection conn = (HttpURLConnection) postURL.openConnection();

            // Set connection parameters. We need to perform input and output,
            // so set both as true.
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Set the content type we are POSTing. We impersonate it as
            // encoded form data
            conn.setRequestProperty("Content-Type", "text/namevalue");
            conn.setRequestProperty("User-Agent", agent);

            //conn.setRequestProperty( "Content-Type", type );
            conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
            conn.setRequestMethod("POST");

            // set the host header
            if (gv_Env.equals("pilot"))
            {
                conn.setRequestProperty("Host", "api-3t.sandbox.paypal.com");
            }
            else
            {
                conn.setRequestProperty("Host", "payflowpro.paypal.com");
            }

            conn.setRequestProperty("X-VPS-CLIENT-TIMEOUT", "45");
            conn.setRequestProperty("X-VPS-REQUEST-ID", unique_id);
            
            System.out.println("**DEBUG : Sending request");
            // get the output stream to POST to.
            DataOutputStream output = new DataOutputStream(conn.getOutputStream());
            output.writeBytes(encodedData);
            output.flush();
            output.close();

            
            // Read input from the input stream.
            DataInputStream in = new DataInputStream(conn.getInputStream());
            int rc = conn.getResponseCode();
            if (rc != -1)
            {
                System.out.println("**DEBUG : Get response.");
                BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String _line = null;
                while (((_line = is.readLine()) != null))
                {
                    respText = respText + _line;
                    System.out.println("**DEBUG : response text : " + respText);
                }
                nvp = deformatNVP(respText);
            }
            return nvp;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            // handle the error here
            return null;
        }
    }

    /*********************************************************************************
     * deformatNVP: Function to break the NVP string into a HashMap
     * 	pPayLoad is the NVP string.
     * returns a HashMap object containing all the name value pairs of the string.
     *********************************************************************************/
    public HashMap deformatNVP(String pPayload)
    {
        HashMap nvp = new HashMap();
        StringTokenizer stTok = new StringTokenizer(pPayload, "&");
        while (stTok.hasMoreTokens())
        {
            StringTokenizer stInternalTokenizer = new StringTokenizer(stTok.nextToken(), "=");
            if (stInternalTokenizer.countTokens() == 2)
            {
                String key = stInternalTokenizer.nextToken();
                String value = stInternalTokenizer.nextToken();
                nvp.put(key.toUpperCase(), value);
            }
        }
        return nvp;
    }

    /*********************************************************************************
     * RedirectURL: Function to redirect the user to the PayPal site
     * 	token is the parameter that was returned by PayPal
     * returns a HashMap object containing all the name value pairs of the string.
     *********************************************************************************/
//    public void RedirectURL(HttpServletResponse response, String token)
//    {
//        String payPalURL = PAYPAL_URL + token;
//
//        //response.sendRedirect( payPalURL );
//        response.setStatus(302);
//        response.setHeader("Location", payPalURL);
//        response.setHeader("Connection", "close");
//    }
//end class
}
