***********************************************
Zyter RPM SSO - Launching zyter RPM from SF
***********************************************
***************
RPMSample.cmp
***************

<aura:component implements="force:appHostable,flexipage:availableForAllPageTypes,flexipage:availableForRecordHome,force:hasRecordId" access="global" controller="RPMcallout" >
    <aura:attribute name="accesstoken" type="String" />
    <aura:attribute name="rpmResponse" type="String" />
    <div>
        <lightning:button label="Access Token" title="Neutral action" onclick="{!c.getAccessToken }"/>        
        {!v.accesstoken}
    </div>
    <div class="slds-hidden">
        <lightning:button label="Get RPM Response" title="Neutral action" onclick="{!c.getRPMresponse }"/>     
        {!v.rpmResponse}
    </div>    
    <div>
        <lightning:button label="Zyter RPM" title="Neutral action" onclick="{!c.navigateZyterRPM }"/>     
    </div>
</aura:component>

<!-- RPMCalloutBkp1703 file Code changes to the master branch to form a new branch - Team member1 -->

***********************
RPMSampleController.js
***********************

({
    getAccessToken : function(component, event, helper) {
        var action = component.get("c.getToken");
        action.setCallback(this, function(response){
            var result = response.getReturnValue();            
            component.set("v.accesstoken",result); 
        });        
        $A.enqueueAction(action);
    },
    getRPMresponse : function(component, event, helper) {  
        
        var tokenParam = component.get("v.accesstoken");        
        var action1 = component.get("c.getCallout");
        action1.setParams({ atkn : tokenParam });
        action1.setCallback(this, function(response){
            var result = response.getReturnValue();            
            component.set("v.rpmResponse",result); 
        });        
        $A.enqueueAction(action1);
    },
    
    navigateZyterRPM : function(component, event, helper) {        
        var accessToken = component.get("v.accesstoken");        
        var zyterURL="https://rpmdev.zyter.net/Telehealth/Oauth2AuthorizeSfc?code="+accessToken+"&providerID=sfcprov1&patientID=sfcpat1@yopmail.com";  
        window.open(zyterURL);
    }
})

****************
RPMcallout.apxc
*****************

public class RPMcallout {
    
    public String result {set; get;}
    public String accessToken {set;get;}
    
    @AuraEnabled
    public static String getToken(){
        String aToken;    
        String endpoint='https://authorizeclients.auth.us-east-1.amazoncognito.com/oauth2/token';
        String clientId= '7bofblo9lp844du49a5srpn0ts' ;
        String clientSecret= '1759rjoacj884jeqtoicstqtr4ngdeqm878j1vkbqmm6r8ivka4f';
        
        string mergedString = clientId+':'+clientSecret;
        System.debug('mergedString::'+mergedString);
        Blob data = Blob.valueOf(mergedString);
        System.debug('data::'+data);
        String encryptedString = EncodingUtil.base64Encode(data);  
        System.debug('Encrypted String::'+encryptedString);        
        
        String body = 'grant_type=client_credentials';
        body = body +'&scope=com.authorize.clients/apis.read';
        
        System.debug('body::'+body);
        Http p = new Http();        
        HttpRequest request = new HttpRequest();        
        request.setEndpoint(endpoint);
        request.setMethod('POST');
        request.setHeader('Authorization','Basic '+encryptedString);
        System.debug('Authorization key::'+request.getHeader('Authorization'));
        request.setBody(body);
        
        HttpResponse response = p.send(request);
        System.debug('response.getStatusCode::'+response.getStatusCode());
        if(response.getStatusCode() == 200) {
            System.debug('ACCESS TOKEN::'+response.getBody());
        }
        
        string result = response.getBody();
        System.JSONParser jp= JSON.createParser(result);
        while(jp.nextToken()!=null){
            if(jp.getText()=='access_token'){
                jp.nextToken();               
                atoken=jp.getText();
            }
        }       
        return atoken;
    }
    @AuraEnabled
    public static String getCallout(string atkn) {
        System.debug('aktn::'+atkn);        
        string apiResonse;
        
        Http http = new Http();
        HttpRequest request = new HttpRequest();
        request.setEndpoint('http://44.194.114.245:8080/ZyterAPIWrapper/patient/hello');
        request.setMethod('GET');
        request.setHeader('Authorization', 'Bearer '+''+atkn);       
        
        HttpResponse response = http.send(request);  
        System.debug('response.getStatusCode::'+response.getStatusCode());
        if(response.getStatusCode() == 200) {
            System.debug('API Respones::'+response.getBody());
        }
        apiResonse = response.getBody();
        return apiResonse;
    } 
}
