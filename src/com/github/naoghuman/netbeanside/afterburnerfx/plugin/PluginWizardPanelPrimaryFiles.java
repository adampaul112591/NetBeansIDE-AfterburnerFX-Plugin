/*
 * Copyright (C) 2015 Naoghuman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.naoghuman.netbeanside.afterburnerfx.plugin;

import com.github.naoghuman.netbeanside.afterburnerfx.plugin.support.PluginSupport;
import com.github.naoghuman.netbeanside.afterburnerfx.plugin.support.SourceGroupSupport;
import com.github.naoghuman.netbeanside.afterburnerfx.plugin.support.IPluginSupport;
import java.beans.PropertyChangeSupport;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

public class PluginWizardPanelPrimaryFiles implements WizardDescriptor.Panel<WizardDescriptor>, IPluginSupport {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    private final boolean isMaven;
    
    private final Project project;
    private final SourceGroupSupport sourceGroupSupport;
    
    private PropertyChangeSupport propertyChangeSupport;
    private WizardDescriptor settings;
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private PluginVisualPanelPrimaryFiles component;

    /**
     * Creates new form AfterburnerVisualPanel1
     */
    public PluginWizardPanelPrimaryFiles(Project project, SourceGroupSupport sourceGroupSupport, boolean isMaven) {
        this.project = project;
        this.sourceGroupSupport = sourceGroupSupport;
        this.isMaven = isMaven;
        
        propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    @Override
    public PluginVisualPanelPrimaryFiles getComponent() {
        if (component == null) {
            component = new PluginVisualPanelPrimaryFiles(project, sourceGroupSupport, changeSupport, isMaven);
            propertyChangeSupport.addPropertyChangeListener(component);
        }
        
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        if (!PluginSupport.isValidBaseName(component.getBaseName())) {
            PluginSupport.setInfoMessage(MSG_INFO__FILE_NAME_ISNT_VALID, settings);
            propertyChangeSupport.firePropertyChange(PROP__SHOW_INFORMATION_CREATE_FOLLOWING_FILES, null, Boolean.FALSE);
            
            return false;
        }
        
        if (!PluginSupport.isBaseNameContainsWrongFileNameChars(component.getBaseName())) {
            PluginSupport.setErrorMessage(MSG_ERROR__FILE_NAME_CONTAINS_WRONG_CHARS, settings);
            propertyChangeSupport.firePropertyChange(PROP__SHOW_INFORMATION_CREATE_FOLLOWING_FILES, null, Boolean.FALSE);
            
            return false;
        }
          
        if (!PluginSupport.isValidPackageName(component.getPackageName())) {
            PluginSupport.setErrorMessage(MSG_ERROR__PACKAGE_NAME_ISNT_VALID, settings);
            propertyChangeSupport.firePropertyChange(PROP__SHOW_INFORMATION_CREATE_FOLLOWING_FILES, null, Boolean.FALSE);
            
            return false;
        }
        
        if (!PluginSupport.isValidPackage(component.getLocationFolder(), component.getPackageName())) {
            PluginSupport.setErrorMessage(MSG_ERROR__PACKAGE_ISNT_FOLDER, settings);
            propertyChangeSupport.firePropertyChange(PROP__SHOW_INFORMATION_CREATE_FOLLOWING_FILES, null, Boolean.FALSE);
            
            return false;
        }
        
        if (!PluginSupport.isValidBaseNameAndPackage(component.getBaseName(), component.getLocationFolder(), component.getPackageName())) {
            PluginSupport.setWarningMessage(MSG_WARNING__FILE_AND_PACKAGE_NAME_ARENT_EQUALS, settings);
            propertyChangeSupport.firePropertyChange(PROP__SHOW_INFORMATION_CREATE_FOLLOWING_FILES, null, Boolean.FALSE);
            
            return false;
        }
        
        final String errorMessage = PluginSupport.canUseFileName(component.getLocationFolder(), component.getPackageFileName(), 
                component.getBaseName(), TEMPLATE_PARAMETER__FXML);
        if (errorMessage != null) {
            settings.getNotificationLineSupport().setErrorMessage(errorMessage);
            propertyChangeSupport.firePropertyChange(PROP__SHOW_INFORMATION_CREATE_FOLLOWING_FILES, null, Boolean.FALSE);
            
            return false;
        }
        
        PluginSupport.clearMessages(settings);
        propertyChangeSupport.firePropertyChange(PROP__SHOW_INFORMATION_CREATE_FOLLOWING_FILES, null, Boolean.TRUE);
            
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        this.settings = settings;
        
        final FileObject preselectedFolder = Templates.getTargetFolder(settings);
        component.initValues(Templates.getTemplate(settings), preselectedFolder);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        NbPreferences.forModule(PluginWizardIterator.class).put(PROP__FILENAME_CHOOSEN, getComponent().getBaseName());
        NbPreferences.forModule(PluginWizardIterator.class).put(PROP__CHOOSEN_PACKAGE, getComponent().getPackageName());
    }

}
