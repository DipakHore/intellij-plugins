package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.Location;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.JavaRuntimeConfigurationProducerBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

/**
 * @author Andrey.Vokin
 * @since 8/6/12
 */
public class CucumberJavaRunConfigurationProducer extends JavaRuntimeConfigurationProducerBase implements Cloneable {
  private PsiElement mySourceElement;

  protected CucumberJavaRunConfigurationProducer() {
    super(CucumberJavaRunConfigurationType.getInstance());
  }

  @Override
  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Override
  protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
    mySourceElement = location.getPsiElement();
    if (!isApplicable(location.getPsiElement(), context.getModule())) {
      return null;
    }

    return createConfiguration(location, context);
  }

  private RunnerAndConfigurationSettings createConfiguration(final Location location, final ConfigurationContext context) {
    final Project project = context.getProject();
    final RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(project, context);
    final CucumberJavaRunConfiguration configuration = (CucumberJavaRunConfiguration)settings.getConfiguration();
    final VirtualFile file = mySourceElement.getContainingFile().getVirtualFile();
    assert file != null : mySourceElement.getContainingFile();
    configuration.setProgramParameters(file.getPath() + " --glue cucumber --format org.jetbrains.plugins.cucumber.java.run.CucumberJavaSMFormatter --monochrome");
    configuration.MAIN_CLASS_NAME = "cucumber.cli.Main";
    if (mySourceElement instanceof PsiNamedElement) {
      if (mySourceElement instanceof GherkinFile) {
        configuration.setName(((GherkinFile)mySourceElement).getVirtualFile().getNameWithoutExtension());
      } else {
        configuration.setName(((PsiNamedElement)mySourceElement).getName());
      }
    }

    setupConfigurationModule(context, configuration);
    JavaRunConfigurationExtensionManager.getInstance().extendCreatedConfiguration(configuration, location);
    return settings;
  }

  @Override
  public int compareTo(Object o) {
    return PREFERED;
  }

  protected boolean isApplicable(PsiElement locationElement, final Module module) {
    return locationElement instanceof GherkinFile;
  }
}