package services;

import javax.inject.Inject;

import models.Documento;
import models.Firmante;
import org.junit.BeforeClass;

import platino.InfoCert;
import play.modules.guice.InjectSupport;
import properties.PropertyPlaceholder;

import services.filesystem.FileSystemFirmaServiceImpl;
import services.platino.PlatinoFirmaServiceImpl;

@InjectSupport
public class PlatinoFirmaServiceTest extends FirmaServiceTest {

    @Inject
    static PropertyPlaceholder propertyPlaceholder;
    
    @BeforeClass
    public static void beforeClass(){
        //firmaService = new PlatinoFirmaServiceImpl(propertyPlaceholder);
        //hasConnection = firmaService.isConfigured();
    }

    @Override
    protected void assertFirmaFirmanteNoEntreFirmantes() {

    }

    @Override
    protected void setMocksImplFirmanteNoEntreFirmantes(String firma) {

    }

    @Override
    protected void setMocksImplFirmanteYaHaFirmado(String firma, Firmante firmante) {

    }

    @Override
    protected void assertFirmanteYaHaFirmado() {

    }

    @Override
    protected void assertFirmanteDocumentoNoSolicitado() {

    }

    @Override
    protected void setMocksImplFirmanteDocumentoNoSolicitado(String firma, Firmante firmante, String valorDocumento) {

    }

    @Override
    protected void assertFirmarFirmaValida(String firma, Firmante firmante, Documento documento) {

    }

    @Override
    protected void setMocksImplFirmarFirmaValida(Documento documento, String firma, Firmante firmante, String valorDocumento) {

    }

    @Override
    public void asssertValidCertificado(InfoCert certificado) {
        // TODO comprobar la información del certificado de la ACIISI
    }
    
}
